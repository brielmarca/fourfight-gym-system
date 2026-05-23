package com.gym.service;

import com.gym.entity.Membership;
import com.gym.entity.Payment;
import com.gym.entity.StripeWebhookEvent;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PaymentRepository;
import com.gym.repository.StripeWebhookEventRepository;
import com.gym.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class StripeWebhookService {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookService.class);

    private final MembershipRepository membershipRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final StripeWebhookEventRepository webhookEventRepository;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    public StripeWebhookService(
            MembershipRepository membershipRepository,
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            StripeWebhookEventRepository webhookEventRepository) {
        this.membershipRepository = membershipRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.webhookEventRepository = webhookEventRepository;
    }

    @Transactional
    public Event handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature", e);
            throw new BusinessRuleException("Invalid webhook signature");
        }

        String eventId = event.getId();
        if (webhookEventRepository.existsByEventId(eventId)) {
            log.info("Webhook event already processed: {}", eventId);
            return event;
        }

        StripeWebhookEvent webhookEvent = StripeWebhookEvent.builder()
                .eventId(eventId)
                .eventType(event.getType())
                .payload(payload)
                .build();

        try {
            processEvent(event);
            webhookEvent.markProcessed();
        } catch (Exception e) {
            log.error("Failed to process webhook event: {}", eventId, e);
            webhookEvent.markFailed(e.getMessage());
            throw e;
        } finally {
            webhookEventRepository.save(webhookEvent);
        }

        return event;
    }

    private void processEvent(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "customer.subscription.created" -> handleSubscriptionCreated(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            default -> log.info("Unhandled Stripe event type: {}", event.getType());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = deserializer.getObject().map(obj -> (Session) obj).orElse(null);

        if (session == null) {
            log.error("Failed to deserialize checkout session");
            return;
        }

        String sessionId = session.getId();
        String subscriptionId = session.getSubscription();

        Membership membership = membershipRepository
                .findByStripeCheckoutSessionId(sessionId)
                .orElse(null);

        if (membership == null) {
            log.warn("No pending membership found for session: {}", sessionId);
            return;
        }

        membership.setStripeSubscriptionId(subscriptionId);

        if (!isPortugalCustomer(session)) {
            membership.setStatus(Membership.MembershipStatus.PENDING_APPROVAL);
            membership.setAutoRenew(false);
            membershipRepository.save(membership);
            log.warn("Stripe checkout completed with non-PT billing country. Membership moved to pending approval: {}", membership.getId());
            return;
        }

        membership.setStatus(Membership.MembershipStatus.PENDING_PAYMENT);
        membership.setAutoRenew(true);

        if (session.getExpiresAt() != null) {
            membership.setCurrentPeriodEnd(
                    LocalDate.ofInstant(Instant.ofEpochSecond(session.getExpiresAt()), java.time.ZoneId.systemDefault())
            );
        }

        membershipRepository.save(membership);
        log.info("Membership linked to Stripe subscription after checkout: {} (subscription: {})", membership.getId(), subscriptionId);
    }

    private void handleSubscriptionCreated(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Subscription subscription = deserializer.getObject().map(obj -> (Subscription) obj).orElse(null);

        if (subscription == null) {
            log.error("Failed to deserialize subscription");
            return;
        }

        Membership membership = membershipRepository
            .findByStripeSubscriptionId(subscription.getId())
            .orElse(null);

        if (membership == null) {
            return;
        }

        membership.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());
        membershipRepository.save(membership);
    }

    private void handleInvoicePaid(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Invoice invoice = deserializer.getObject().map(obj -> (Invoice) obj).orElse(null);

        if (invoice == null) {
            log.error("Failed to deserialize invoice");
            return;
        }

        String subscriptionId = invoice.getSubscription();
        if (subscriptionId == null) return;

        Membership membership = membershipRepository
                .findByStripeSubscriptionId(subscriptionId)
                .orElse(null);

        if (membership == null) {
            log.warn("No membership found for subscription: {}", subscriptionId);
            return;
        }

        if (membership.getStatus() == Membership.MembershipStatus.PENDING_APPROVAL) {
            log.warn("Invoice paid for non-PT restricted membership; keeping pending approval: {}", membership.getId());
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(invoice.getAmountPaid()).divide(BigDecimal.valueOf(100));

        Payment payment = Payment.builder()
                .user(membership.getUser())
                .membership(membership)
                .amount(amount)
                .currency(invoice.getCurrency() != null ? invoice.getCurrency().toUpperCase() : "EUR")
                .method(Payment.PaymentMethod.STRIPE)
                .status(Payment.PaymentStatus.COMPLETED)
                .gatewayRef(invoice.getId())
                .stripePaymentIntentId(invoice.getPaymentIntent())
                .stripeInvoiceId(invoice.getId())
                .paidAt(Instant.ofEpochSecond(invoice.getCreated()).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .build();

        paymentRepository.save(payment);

        LocalDate startDate = LocalDate.now();
        if (invoice.getPeriodStart() != null) {
            startDate = LocalDate.ofInstant(Instant.ofEpochSecond(invoice.getPeriodStart()), java.time.ZoneId.systemDefault());
        }

        if (invoice.getPeriodEnd() != null) {
            membership.setCurrentPeriodEnd(
                    LocalDate.ofInstant(Instant.ofEpochSecond(invoice.getPeriodEnd()), java.time.ZoneId.systemDefault())
            );
            membership.setEndDate(membership.getCurrentPeriodEnd());
        }
        if (invoice.getPeriodStart() != null) {
            membership.setCurrentPeriodStart(
                    LocalDate.ofInstant(Instant.ofEpochSecond(invoice.getPeriodStart()), java.time.ZoneId.systemDefault())
            );
        }

        membership.setStartDate(startDate);

        membership.setStatus(Membership.MembershipStatus.ACTIVE);
        membershipRepository.save(membership);

        log.info("Invoice paid for membership: {} (amount: {})", membership.getId(), amount);
    }

    private void handleInvoicePaymentFailed(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Invoice invoice = deserializer.getObject().map(obj -> (Invoice) obj).orElse(null);

        if (invoice == null) {
            log.error("Failed to deserialize invoice");
            return;
        }

        String subscriptionId = invoice.getSubscription();
        if (subscriptionId == null) return;

        Membership membership = membershipRepository
                .findByStripeSubscriptionId(subscriptionId)
                .orElse(null);

        if (membership == null) {
            log.warn("No membership found for subscription: {}", subscriptionId);
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(invoice.getAmountDue()).divide(BigDecimal.valueOf(100));

        Payment payment = Payment.builder()
                .user(membership.getUser())
                .membership(membership)
                .amount(amount)
                .currency(invoice.getCurrency() != null ? invoice.getCurrency().toUpperCase() : "EUR")
                .method(Payment.PaymentMethod.STRIPE)
                .status(Payment.PaymentStatus.FAILED)
                .gatewayRef(invoice.getId())
                .gatewayResponse("Payment failed: " + (invoice.getLastFinalizationError() != null ? invoice.getLastFinalizationError().getMessage() : "unknown"))
                .build();

        paymentRepository.save(payment);
        membership.setStatus(Membership.MembershipStatus.SUSPENDED);
        membershipRepository.save(membership);
        log.warn("Invoice payment failed for membership: {}", membership.getId());
    }

    private void handleSubscriptionDeleted(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Subscription subscription = deserializer.getObject().map(obj -> (Subscription) obj).orElse(null);

        if (subscription == null) {
            log.error("Failed to deserialize subscription");
            return;
        }

        String subscriptionId = subscription.getId();
        Membership membership = membershipRepository
                .findByStripeSubscriptionId(subscriptionId)
                .orElse(null);

        if (membership == null) {
            log.warn("No membership found for deleted subscription: {}", subscriptionId);
            return;
        }

        membership.setStatus(Membership.MembershipStatus.CANCELLED);
        membership.setAutoRenew(false);
        membershipRepository.save(membership);

        log.info("Membership cancelled via Stripe subscription deletion: {}", membership.getId());
    }

    private void handleSubscriptionUpdated(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Subscription subscription = deserializer.getObject().map(obj -> (Subscription) obj).orElse(null);

        if (subscription == null) {
            log.error("Failed to deserialize subscription");
            return;
        }

        String subscriptionId = subscription.getId();
        Membership membership = membershipRepository
                .findByStripeSubscriptionId(subscriptionId)
                .orElse(null);

        if (membership == null) {
            log.warn("No membership found for updated subscription: {}", subscriptionId);
            return;
        }

        membership.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());

        if (subscription.getCurrentPeriodEnd() != null) {
            membership.setCurrentPeriodEnd(
                    LocalDate.ofInstant(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()), java.time.ZoneId.systemDefault())
            );
        }

        membershipRepository.save(membership);
        log.info("Membership subscription updated: {} (cancelAtPeriodEnd: {})", membership.getId(), subscription.getCancelAtPeriodEnd());
    }

    private boolean isPortugalCustomer(Session session) {
        String country = null;

        if (session.getCustomerDetails() != null
                && session.getCustomerDetails().getAddress() != null
                && session.getCustomerDetails().getAddress().getCountry() != null) {
            country = session.getCustomerDetails().getAddress().getCountry();
        }

        if ((country == null || country.isBlank()) && session.getCustomer() != null) {
            try {
                Customer customer = Customer.retrieve(session.getCustomer());
                if (customer.getAddress() != null) {
                    country = customer.getAddress().getCountry();
                }
            } catch (StripeException e) {
                log.warn("Unable to retrieve Stripe customer country for session {}", session.getId(), e);
            }
        }

        return country != null && "PT".equalsIgnoreCase(country);
    }
}
