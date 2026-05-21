package com.example.fawrypaymentrouting.payment.dto;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyTransactionHistoryResponseDto(
        LocalDate date,
        BigDecimal totalAmountProcessed,
        BigDecimal totalCommissionCharged,
        List<GatewaySummaryDto> gatewayBreakdown,
        Page<TransactionLogDto> transactions
) {}
