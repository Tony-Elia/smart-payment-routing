package com.example.fawrypaymentrouting.payment.service;

import com.example.fawrypaymentrouting.payment.dto.*;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
import com.example.fawrypaymentrouting.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private final PaymentTransactionRepository repository;

    @Transactional(readOnly = true)
    public DailyTransactionHistoryResponseDto getDailyHistory(UUID billerId, LocalDate date, Pageable pageable) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 1. Let the DB group and sum the data per gateway
        List<GatewaySummaryDto> gatewayBreakdown = repository
                .getDailyGatewaySummaries(billerId, startOfDay, endOfDay);
        log.info("Gateway breakdown for biller {} on {}: {}", billerId, date, gatewayBreakdown);
        // 2. Calculate Master Totals by summing gateway rows (O(1) essentially)
        BigDecimal totalAmount = gatewayBreakdown.stream()
                .map(GatewaySummaryDto::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCommission = gatewayBreakdown.stream()
                .map(GatewaySummaryDto::totalCommission) // Matches your custom DTO field name
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Fetch only the requested PAGE of transactions
        Page<PaymentTransaction> pagedTransactions = repository
                .findAllByBillerIdAndCreatedAtBetween(billerId, startOfDay, endOfDay, pageable);
        log.info("Fetched {} transactions for biller {} on {}", pagedTransactions.getNumberOfElements(), billerId, date);
        // 4. Map the Page of Entities to a Page of DTOs natively
        Page<TransactionLogDto> logs = pagedTransactions.map(t -> new TransactionLogDto(
                t.getId(),
                t.getAmount(),
                t.getCommissionApplied(),
                t.getGateway().getName(),
                t.getStatus().name(),
                t.getCreatedAt()
        ));

        // 5. Assemble and return
        return new DailyTransactionHistoryResponseDto(
                date,
                totalAmount,
                totalCommission,
                gatewayBreakdown,
                logs
        );
    }
}