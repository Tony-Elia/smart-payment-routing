package com.example.fawrypaymentrouting.gateway.dto;
import com.example.fawrypaymentrouting.gateway.model.AvailabilityWindow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
public record GatewayRequestDto(
    @NotBlank String name,
    @PositiveOrZero BigDecimal commissionFixed,
    @PositiveOrZero BigDecimal commissionPercentage,
    @PositiveOrZero BigDecimal dailyLimit,
    @PositiveOrZero BigDecimal minTransaction,
    BigDecimal maxTransaction,
    @PositiveOrZero BigDecimal processingTimeInHours,
    Boolean available247,
    @Valid List<AvailabilityWindow> availabilityWindows,
    Boolean active
) {
    public GatewayRequestDto {
        if (available247 == null) {
            available247 = false;
        }
        if (active == null) {
            active = true;
        }
    }
}
