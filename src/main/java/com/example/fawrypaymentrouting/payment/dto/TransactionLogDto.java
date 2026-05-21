package com.example.fawrypaymentrouting.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionLogDto(
        UUID transactionId,
        BigDecimal amount,
        BigDecimal commission,
        String gatewayName,
        String status,
        LocalDateTime timestamp
) {}
