package com.example.fawrypaymentrouting.gateway.dto;
import com.example.fawrypaymentrouting.gateway.model.AvailabilityWindow;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record GatewayResponseDto(
    UUID id,
    String name,
    BigDecimal commissionFixed,
    BigDecimal commissionPercentage,
    BigDecimal dailyLimit,
    BigDecimal minTransaction,
    BigDecimal maxTransaction,
    BigDecimal processingTimeInHours,
    boolean available247,
    List<AvailabilityWindow> availabilityWindows,
    boolean active
) {}
