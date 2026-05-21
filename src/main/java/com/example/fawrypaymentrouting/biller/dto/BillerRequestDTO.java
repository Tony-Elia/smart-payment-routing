package com.example.fawrypaymentrouting.biller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record BillerRequestDTO(
        @NotBlank String name
) {}
