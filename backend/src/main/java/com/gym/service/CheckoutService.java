package com.gym.service;

import com.gym.dto.request.CheckoutRequest;
import com.gym.dto.request.PaymentFormRequest;
import com.gym.dto.response.CheckoutResponse;
import com.gym.security.GymUserDetailsService.JwtUserPrincipal;

public interface CheckoutService {
    CheckoutResponse processCheckout(CheckoutRequest request, JwtUserPrincipal principal);
    CheckoutResponse processPaymentForm(String paymentId, PaymentFormRequest formRequest, JwtUserPrincipal principal);
    CheckoutResponse getCheckoutStatus(String paymentId, JwtUserPrincipal principal);
}
