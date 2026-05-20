package com.example.fawrypaymentrouting.gateway.dto;
import com.example.fawrypaymentrouting.gateway.model.AvailabilityWindow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
public record GatewayRequestDto(
    @NotBlank String name,
    @NotNull BigDecimal commissionFixed,
    @NotNull BigDecimal commissionPercentage,
    @NotNull BigDecimal dailyLimit,
    @NotNull BigDecimal minTransaction,
    BigDecimal maxTransaction,
    @NotNull BigDecimal processingTimeInHours,
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
