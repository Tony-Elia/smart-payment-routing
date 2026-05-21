package com.example.fawrypaymentrouting.payment.dto;

import com.example.fawrypaymentrouting.payment.model.Urgency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentTransactionRequestDto(
        @NotNull(message = "Biller ID must not be null")
        UUID billerId,

        @NotNull(message = "Gateway ID must not be null")
        UUID gatewayId,

        @NotNull(message = "Amount must not be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Urgency must not be null")
        Urgency urgency,

        UUID parentTransactionId
) {}
