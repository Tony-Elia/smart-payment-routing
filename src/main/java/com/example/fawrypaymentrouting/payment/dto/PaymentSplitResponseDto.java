package com.example.fawrypaymentrouting.payment.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PaymentSplitResponseDto(
        String selectedGateway,
        UUID selectedGatewayId,
        boolean requiresSplitting,
        List<BigDecimal> splits,
        BigDecimal totalCommission,
        boolean quotaAvailable,
        int splitCount
) {}