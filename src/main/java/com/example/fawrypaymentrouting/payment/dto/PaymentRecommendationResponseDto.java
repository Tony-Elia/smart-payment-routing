package com.example.fawrypaymentrouting.payment.dto;
import java.util.List;
public record PaymentRecommendationResponseDto(
    RecommendedGatewayDto recommendedGateway,
    List<AlternativeGatewayDto> alternatives
) {}
