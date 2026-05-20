package com.example.fawrypaymentrouting.payment.dto;
import java.math.BigDecimal;
import java.util.UUID;
public record AlternativeGatewayDto(
    UUID id,
    BigDecimal estimatedCommission
) {}
