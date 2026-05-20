package com.example.fawrypaymentrouting.biller.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record BillerRequestDTO(
        @NotEmpty String name,
        @PositiveOrZero BigDecimal dailyQuota
) {}
