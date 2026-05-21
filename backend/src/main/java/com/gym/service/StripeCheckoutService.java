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
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductCreateParams;
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

        String stripePriceId;
        try {
            stripePriceId = resolveOrCreateStripePriceId(plan);
        } catch (StripeException e) {
            log.error("Failed to resolve Stripe price for plan {}", plan.getId(), e);
            throw new BusinessRuleException("Failed to prepare Stripe price: " + e.getMessage());
        }

        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .setSuccessUrl(frontendSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendCancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(stripePriceId)
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
                .stripePriceId(stripePriceId)
                .status(Membership.MembershipStatus.PENDING_PAYMENT)
                .autoRenew(false)
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

    private String resolveOrCreateStripePriceId(Plan plan) throws StripeException {
        String existingPriceId = plan.getStripePriceId();
        if (existingPriceId != null && !existingPriceId.isBlank()) {
            Price existing = Price.retrieve(existingPriceId);
            if (isEurMonthlyPrice(existing, plan)) {
                return existingPriceId;
            }
            log.warn("Existing Stripe price {} for plan {} is not EUR monthly with expected amount", existingPriceId, plan.getId());
        }

        String lookupKey = "fourfight_plan_" + plan.getId() + "_eur_monthly";
        PriceListParams listParams = PriceListParams.builder()
            .addLookupKey(lookupKey)
            .setActive(true)
            .setLimit(1L)
            .build();
        var priceList = Price.list(listParams);
        if (!priceList.getData().isEmpty()) {
            Price matched = priceList.getData().get(0);
            if (isEurMonthlyPrice(matched, plan)) {
                persistStripeIds(plan, matched.getProduct(), matched.getId());
                return matched.getId();
            }
        }

        String productId = ensureStripeProduct(plan);
        long unitAmount = plan.getPrice().movePointRight(2).longValueExact();

        PriceCreateParams createParams = PriceCreateParams.builder()
            .setCurrency("eur")
            .setUnitAmount(unitAmount)
            .setProduct(productId)
            .setLookupKey(lookupKey)
            .setRecurring(
                PriceCreateParams.Recurring.builder()
                    .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                    .build()
            )
            .putMetadata("planId", plan.getId().toString())
            .build();

        Price created = Price.create(createParams);
        persistStripeIds(plan, productId, created.getId());
        return created.getId();
    }

    private boolean isEurMonthlyPrice(Price price, Plan plan) {
        if (price == null || price.getRecurring() == null || price.getUnitAmount() == null) {
            return false;
        }
        boolean eur = "eur".equalsIgnoreCase(price.getCurrency());
        boolean monthly = "month".equalsIgnoreCase(price.getRecurring().getInterval());
        long expected = plan.getPrice().movePointRight(2).longValueExact();
        return eur && monthly && expected == price.getUnitAmount();
    }

    private String ensureStripeProduct(Plan plan) throws StripeException {
        if (plan.getStripeProductId() != null && !plan.getStripeProductId().isBlank()) {
            return plan.getStripeProductId();
        }

        ProductCreateParams productParams = ProductCreateParams.builder()
            .setName(plan.getName() + " - 4Four Fight Academy")
            .setDescription(plan.getDescription())
            .putMetadata("planId", plan.getId().toString())
            .build();
        Product product = Product.create(productParams);
        plan.setStripeProductId(product.getId());
        planRepository.save(plan);
        return product.getId();
    }

    private void persistStripeIds(Plan plan, String productId, String priceId) {
        plan.setStripeProductId(productId);
        plan.setStripePriceId(priceId);
        planRepository.save(plan);
    }
}
