package com.example.fawrypaymentrouting.payment.dto;
import com.example.fawrypaymentrouting.payment.model.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRecommendationRequestDto(
    @NotNull UUID billerId,
    @NotNull @Positive BigDecimal amount,
    @NotNull Urgency urgency
) {}
