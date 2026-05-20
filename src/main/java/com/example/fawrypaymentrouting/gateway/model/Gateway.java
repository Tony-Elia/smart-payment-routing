package com.example.fawrypaymentrouting.gateway.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "gateways")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Gateway {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    // Commission = commissionFixed + (amount * commissionPercentage / 100)
    @Column(name = "commission_fixed", nullable = false)
    private BigDecimal commissionFixed;

    @Column(name = "commission_percentage", nullable = false)
    private BigDecimal commissionPercentage;

    @Column(name = "daily_limit", nullable = false)
    private BigDecimal dailyLimit;

    @Column(name = "min_transaction", nullable = false)
    private BigDecimal minTransaction;

    // Null means "No Limit"
    @Column(name = "max_transaction")
    private BigDecimal maxTransaction;

    @Column(name = "processing_time_in_hours", nullable = false)
    private BigDecimal processingTimeInHours;

    // Simplified availability tracking
    @Column(name = "available_24_7", nullable = false)
    private boolean available247 = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "availability_windows", columnDefinition = "jsonb")
    private List<AvailabilityWindow> availabilityWindows = new ArrayList<>();

    // Status flag to enable/disable gateway without deleting
    @Column(name = "active", nullable = false)
    private boolean active = true;

    // --- Business Logic Methods ---

    public boolean isAvailableAt(LocalDateTime time) {
        if (available247) return true;
        if (availabilityWindows == null || availabilityWindows.isEmpty()) return false;

        DayOfWeek currentDay = time.getDayOfWeek();
        LocalTime currentTime = time.toLocalTime();

        for (AvailabilityWindow window : availabilityWindows) {
            if (window.dayOfWeek() == currentDay) {
                // Must be >= startTime and <= endTime
                if (!currentTime.isBefore(window.startTime()) && !currentTime.isAfter(window.endTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    public BigDecimal calculateCommission(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        BigDecimal percentageFee = amount.multiply(commissionPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return commissionFixed.add(percentageFee).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isValidAmount(BigDecimal amount) {
        if (amount == null) return false;
        if (amount.compareTo(minTransaction) < 0) return false;
        return maxTransaction == null || amount.compareTo(maxTransaction) <= 0;
    }
}
