package com.gym.service;

import com.gym.dto.response.StripeCheckoutResponse;
import com.gym.entity.Membership;
import com.gym.entity.Plan;
import com.gym.entity.User;
import com.gym.exception.BusinessRuleException;
import com.gym.exception.ResourceNotFoundException;
import com.gym.repository.MembershipRepository;
import com.gym.repository.PlanRepository;
import com.gym.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class StripeCheckoutService {

    private static final Logger log = LoggerFactory.getLogger(StripeCheckoutService.class);

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final MembershipRepository membershipRepository;

    @Value("${stripe.frontend-success-url:http://localhost:5173/membership/success}")
    private String frontendSuccessUrl;

    @Value("${stripe.frontend-cancel-url:http://localhost:5173/plans}")
    private String frontendCancelUrl;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public StripeCheckoutService(
            UserRepository userRepository,
            PlanRepository planRepository,
            MembershipRepository membershipRepository) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public StripeCheckoutResponse createCheckoutSession(UUID userId, UUID planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));

        if (!plan.getIsActive()) {
            throw new BusinessRuleException("Plan is not active");
        }

        String checkoutCurrency = plan.getCurrency() == null ? "" : plan.getCurrency().trim().toLowerCase(Locale.ROOT);
        if (!"eur".equals(checkoutCurrency)) {
            throw new BusinessRuleException("Checkout is only available in EUR");
        }

        String customerId;
        try {
            customerId = getOrCreateStripeCustomer(user);
        } catch (StripeException e) {
            log.error("Failed to get or create Stripe customer for user {}", user.getId(), e);
            throw new BusinessRuleException("Failed to initialize payment customer: " + e.getMessage());
        }

        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setCurrency("eur")
                .setSuccessUrl(frontendSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(plan.getName() + " - 4Four Fight Academy")
                                                                .build()
                                                )
                                                .setUnitAmountDecimal(plan.getPrice().multiply(java.math.BigDecimal.valueOf(100)))
                                                .setCurrency("eur")
                                                .setRecurring(
                                                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("userId", user.getId().toString())
                .putMetadata("planId", plan.getId().toString())
                .putMetadata("membershipType", "new")
                .putExtraParam("adaptive_pricing", java.util.Map.of("enabled", false));

        Session session;
        try {
            session = Session.create(sessionBuilder.build());
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new BusinessRuleException("Failed to create payment session: " + e.getMessage());
        }

        Membership pendingMembership = Membership.builder()
                .user(user)
                .plan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(plan.getDurationDays()))
                .stripeCheckoutSessionId(session.getId())
                .stripePriceId(plan.getStripePriceId())
                .status(Membership.MembershipStatus.ACTIVE)
                .autoRenew(true)
                .build();

        membershipRepository.save(pendingMembership);

        return new StripeCheckoutResponse(session.getId(), session.getUrl());
    }

    private String getOrCreateStripeCustomer(User user) throws StripeException {
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isBlank()) {
            return user.getStripeCustomerId();
        }

        CustomerCreateParams customerParams = CustomerCreateParams.builder()
                .setName(user.getName())
                .setEmail(user.getEmail())
                .putMetadata("userId", user.getId().toString())
                .build();

        Customer customer = Customer.create(customerParams);
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);

        return customer.getId();
    }
}
