package com.gym.service;

import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.StripeWebhookEvent;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.StripeWebhookEventRepository;
import com.gym.repository.UserRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Address;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    @Mock private MembershipRepository membershipRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private StripeWebhookEventRepository webhookEventRepository;

    private StripeWebhookService stripeWebhookService;

    @BeforeEach
    void setUp() {
        stripeWebhookService = new StripeWebhookService(
                membershipRepository,
                paymentRepository,
                userRepository,
                webhookEventRepository
        );
        ReflectionTestUtils.setField(stripeWebhookService, "webhookSecret", WEBHOOK_SECRET);
    }

    @Test
    void handleWebhook_rejectsInvalidSignature() {
        String payload = "{\"id\":\"evt_invalid\",\"object\":\"event\",\"type\":\"ping\",\"data\":{\"object\":{\"object\":\"charge\"}}}";
        assertThatThrownBy(() -> stripeWebhookService.handleWebhook(payload, "t=1,v1=bad"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Invalid webhook signature");
    }

    @Test
    void handleWebhook_duplicateEvent_isIdempotent() {
        String payload = "{\"id\":\"evt_duplicate\",\"object\":\"event\",\"type\":\"ping\",\"data\":{\"object\":{\"object\":\"charge\"}}}";
        when(webhookEventRepository.existsByEventId("evt_duplicate")).thenReturn(true);

        stripeWebhookService.handleWebhook(payload, validSignature(payload));

        verify(webhookEventRepository, never()).save(any(StripeWebhookEvent.class));
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test
    void checkoutCompleted_nonPt_setsPendingApproval() {
        Membership membership = baseMembership();
        Event event = eventWithSession("cs_non_pt", "sub_non_pt", "ES");
        when(membershipRepository.findByStripeCheckoutSessionId("cs_non_pt")).thenReturn(Optional.of(membership));

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleCheckoutSessionCompleted", event);

        assertThat(membership.getStatus()).isEqualTo(Membership.MembershipStatus.PENDING_APPROVAL);
        assertThat(membership.getAutoRenew()).isFalse();
        assertThat(membership.getStripeSubscriptionId()).isEqualTo("sub_non_pt");
    }

    @Test
    void checkoutCompleted_fallbackByMetadata_linksMembershipWhenSessionIdMisses() {
        Membership membership = baseMembership();
        UUID userId = membership.getUser().getId();
        UUID planId = UUID.randomUUID();
        membership.setPlan(com.gym.entity.Plan.builder().id(planId).name("Plan").price(java.math.BigDecimal.TEN).durationDays(30).build());

        Event event = eventWithSession("cs_meta", "sub_meta", "PT");
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        when(session.getMetadata()).thenReturn(Map.of("userId", userId.toString(), "planId", planId.toString()));
        when(membershipRepository.findByStripeCheckoutSessionId("cs_meta")).thenReturn(Optional.empty());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(membershipRepository.findAllByUserIdAndPlanIdOrderByCreatedAtDesc(userId, planId)).thenReturn(List.of(membership));

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleCheckoutSessionCompleted", event);

        assertThat(membership.getStripeSubscriptionId()).isEqualTo("sub_meta");
        assertThat(membership.getStripeCheckoutSessionId()).isEqualTo("cs_meta");
        verify(membershipRepository, times(1)).save(membership);
    }

    @Test
    void checkoutCompleted_invalidMetadata_failsSafelyWithoutActivation() {
        Event event = eventWithSessionBasic("cs_invalid", "sub_invalid");
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        when(session.getMetadata()).thenReturn(Map.of("userId", "bad", "planId", "also-bad"));
        when(membershipRepository.findByStripeCheckoutSessionId("cs_invalid")).thenReturn(Optional.empty());
        when(session.getClientReferenceId()).thenReturn("invalid-client-ref");

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleCheckoutSessionCompleted", event);

        verify(membershipRepository, never()).save(any(Membership.class));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void invoicePaid_activatesAndSetsPeriodDates() {
        Membership membership = baseMembership();
        membership.setStripeSubscriptionId("sub_1");
        Event event = eventWithInvoicePaid("sub_1", "in_1", 1717200000L, 1719792000L);

        when(membershipRepository.findByStripeSubscriptionId("sub_1")).thenReturn(Optional.of(membership));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleInvoicePaid", event);

        assertThat(membership.getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
        assertThat(membership.getCurrentPeriodStart()).isEqualTo(LocalDate.ofInstant(Instant.ofEpochSecond(1717200000L), ZoneId.systemDefault()));
        assertThat(membership.getCurrentPeriodEnd()).isEqualTo(LocalDate.ofInstant(Instant.ofEpochSecond(1719792000L), ZoneId.systemDefault()));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void invoicePaid_duplicateInvoiceId_doesNotInsertDuplicatePaymentAcrossEvents() {
        Membership membership = baseMembership();
        membership.setStripeSubscriptionId("sub_repeat");
        Event firstEvent = eventWithInvoicePaid("sub_repeat", "in_repeat", 1717200000L, 1719792000L);
        Event secondEvent = eventWithInvoicePaidForDuplicate("sub_repeat", "in_repeat", 1719792000L, 1722470400L);

        when(membershipRepository.findByStripeSubscriptionId("sub_repeat")).thenReturn(Optional.of(membership));
        when(paymentRepository.existsByStripeInvoiceId("in_repeat")).thenReturn(false, true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleInvoicePaid", firstEvent);
        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleInvoicePaid", secondEvent);

        verify(paymentRepository, times(1)).save(any(Payment.class));
        assertThat(membership.getStatus()).isEqualTo(Membership.MembershipStatus.ACTIVE);
    }

    @Test
    void invoicePaid_doesNotActivatePendingApprovalMembership() {
        Membership membership = baseMembership();
        membership.setStatus(Membership.MembershipStatus.PENDING_APPROVAL);
        membership.setStripeSubscriptionId("sub_hold");
        Event event = eventWithInvoicePaidMinimal("sub_hold");
        when(membershipRepository.findByStripeSubscriptionId("sub_hold")).thenReturn(Optional.of(membership));

        ReflectionTestUtils.invokeMethod(stripeWebhookService, "handleInvoicePaid", event);

        assertThat(membership.getStatus()).isEqualTo(Membership.MembershipStatus.PENDING_APPROVAL);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    private Membership baseMembership() {
        return Membership.builder()
                .id(UUID.randomUUID())
                .user(User.builder().id(UUID.randomUUID()).name("Test").email("test@example.com").passwordHash("hash").build())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .status(Membership.MembershipStatus.PENDING_PAYMENT)
                .autoRenew(false)
                .build();
    }

    private Event eventWithSession(String sessionId, String subscriptionId, String country) {
        Event event = eventWithSessionBasic(sessionId, subscriptionId);
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
        Session.CustomerDetails customerDetails = org.mockito.Mockito.mock(Session.CustomerDetails.class);
        Address address = org.mockito.Mockito.mock(Address.class);
        when(session.getCustomerDetails()).thenReturn(customerDetails);
        when(customerDetails.getAddress()).thenReturn(address);
        when(address.getCountry()).thenReturn(country);
        return event;
    }

    private Event eventWithSessionBasic(String sessionId, String subscriptionId) {
        Event event = org.mockito.Mockito.mock(Event.class);
        EventDataObjectDeserializer deserializer = org.mockito.Mockito.mock(EventDataObjectDeserializer.class);
        Session session = org.mockito.Mockito.mock(Session.class);

        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(session.getId()).thenReturn(sessionId);
        when(session.getSubscription()).thenReturn(subscriptionId);
        return event;
    }

    private Event eventWithInvoicePaid(String subscriptionId, String invoiceId, long periodStart, long periodEnd) {
        Event event = org.mockito.Mockito.mock(Event.class);
        EventDataObjectDeserializer deserializer = org.mockito.Mockito.mock(EventDataObjectDeserializer.class);
        Invoice invoice = org.mockito.Mockito.mock(Invoice.class);

        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        when(invoice.getAmountPaid()).thenReturn(4900L);
        when(invoice.getCurrency()).thenReturn("eur");
        when(invoice.getPaymentIntent()).thenReturn("pi_123");
        when(invoice.getId()).thenReturn(invoiceId);
        when(invoice.getCreated()).thenReturn(periodStart);
        when(invoice.getPeriodStart()).thenReturn(periodStart);
        when(invoice.getPeriodEnd()).thenReturn(periodEnd);
        return event;
    }

    private Event eventWithInvoicePaidMinimal(String subscriptionId) {
        Event event = org.mockito.Mockito.mock(Event.class);
        EventDataObjectDeserializer deserializer = org.mockito.Mockito.mock(EventDataObjectDeserializer.class);
        Invoice invoice = org.mockito.Mockito.mock(Invoice.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        return event;
    }

    private Event eventWithInvoicePaidForDuplicate(String subscriptionId, String invoiceId, long periodStart, long periodEnd) {
        Event event = org.mockito.Mockito.mock(Event.class);
        EventDataObjectDeserializer deserializer = org.mockito.Mockito.mock(EventDataObjectDeserializer.class);
        Invoice invoice = org.mockito.Mockito.mock(Invoice.class);

        when(event.getDataObjectDeserializer()).thenReturn(deserializer);
        when(deserializer.getObject()).thenReturn(Optional.of(invoice));
        when(invoice.getSubscription()).thenReturn(subscriptionId);
        when(invoice.getAmountPaid()).thenReturn(4900L);
        when(invoice.getId()).thenReturn(invoiceId);
        when(invoice.getPeriodStart()).thenReturn(periodStart);
        when(invoice.getPeriodEnd()).thenReturn(periodEnd);
        return event;
    }

    private String validSignature(String payload) {
        long timestamp = Instant.now().getEpochSecond();
        String signedPayload = timestamp + "." + payload;
        String signature = hmacSha256Hex(WEBHOOK_SECRET, signedPayload);
        return "t=" + timestamp + ",v1=" + signature;
    }

    private String hmacSha256Hex(String secret, String data) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] digest = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
