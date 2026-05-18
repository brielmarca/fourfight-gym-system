package com.gym.mapper;

import com.gym.dto.request.CreatePaymentRequest;
import com.gym.dto.response.PaymentResponse;
import com.gym.entity.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "membership", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    Payment toEntity(CreatePaymentRequest request);

    default PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.from(payment);
    }

    List<PaymentResponse> toResponseList(List<Payment> payments);
}