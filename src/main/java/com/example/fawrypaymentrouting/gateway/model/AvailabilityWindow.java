package com.example.fawrypaymentrouting.gateway.model;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityWindow(
    @NotNull(message = "Day of week must be provided if not available 24/7")
    DayOfWeek dayOfWeek,
    @NotNull(message = "Start time must be provided if not available 24/7")
    LocalTime startTime,
    @NotNull(message = "End time must be provided if not available 24/7")
    LocalTime endTime
) {}
