package com.example.fawrypaymentrouting.payment.dto;

import com.example.fawrypaymentrouting.payment.model.TransactionStatus;
import com.example.fawrypaymentrouting.payment.model.Urgency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTransactionResponseDto(
        UUID id,
        UUID billerId,
        UUID gatewayId,
        BigDecimal amount,
        BigDecimal commissionApplied,
        Urgency urgency,
        TransactionStatus status,
        LocalDateTime createdAt,
        UUID parentTransactionId
) {}
