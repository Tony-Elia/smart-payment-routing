package com.example.fawrypaymentrouting.gateway.dto;
import com.example.fawrypaymentrouting.gateway.model.AvailabilityWindow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record GatewayRequestDto(
        @NotBlank String name,
        @NotNull @PositiveOrZero BigDecimal commissionFixed,
        @NotNull @PositiveOrZero BigDecimal commissionPercentage,
        @NotNull @Positive BigDecimal dailyLimit,
        @NotNull @PositiveOrZero BigDecimal minTransaction,
        @Positive BigDecimal maxTransaction,
        @NotNull @PositiveOrZero Double processingTimeInHours,
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

    @AssertTrue(message = "Maximum transaction limit must be greater than or equal to the minimum transaction limit")
    private boolean isMinMaxValid() {
        // If maxTransaction is null, there is no ceiling, so the rule passes.
        if (maxTransaction == null || minTransaction == null) {
            return true;
        }
        return maxTransaction.compareTo(minTransaction) >= 0;
    }
}
