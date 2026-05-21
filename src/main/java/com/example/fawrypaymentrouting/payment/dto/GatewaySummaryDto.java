package com.example.fawrypaymentrouting.payment.dto;

import java.math.BigDecimal;

public record GatewaySummaryDto(
        String gatewayName,
        long transactionCount,
        BigDecimal totalAmount,
        BigDecimal totalCommission
) {}
