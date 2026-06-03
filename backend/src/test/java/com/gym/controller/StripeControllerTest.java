package com.gym.controller;

import com.gym.exception.BusinessRuleException;
import com.gym.repository.MembershipRepository;
import com.gym.service.ReceptionCheckoutService;
import com.gym.service.StripeCheckoutService;
import com.gym.service.StripeWebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeControllerTest {

    @Mock private StripeCheckoutService stripeCheckoutService;
    @Mock private ReceptionCheckoutService receptionCheckoutService;
    @Mock private StripeWebhookService stripeWebhookService;
    @Mock private MembershipRepository membershipRepository;

    private StripeController stripeController;

    @BeforeEach
    void setUp() {
        stripeController = new StripeController(
                stripeCheckoutService,
                receptionCheckoutService,
                stripeWebhookService,
                membershipRepository
        );
    }

    @Test
    void handleWebhook_missingStripeSignature_returnsBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{}".getBytes());

        ResponseEntity<Void> response = stripeController.handleWebhook(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(stripeWebhookService, never()).handleWebhook("{}", null);
    }

    @Test
    void handleWebhook_invalidStripeSignature_returnsBadRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent("{}".getBytes());
        request.addHeader("Stripe-Signature", "t=1,v1=bad");
        when(stripeWebhookService.handleWebhook("{}", "t=1,v1=bad"))
                .thenThrow(new BusinessRuleException("Invalid webhook signature"));

        ResponseEntity<Void> response = stripeController.handleWebhook(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(stripeWebhookService).handleWebhook("{}", "t=1,v1=bad");
    }
}
