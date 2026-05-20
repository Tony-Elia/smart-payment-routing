package com.example.fawrypaymentrouting.payment.dto;
import java.math.BigDecimal;
import java.util.UUID;
public record RecommendedGatewayDto(
    UUID id,
    String name,
    BigDecimal estimatedCommission,
    String processingTime
) {}
