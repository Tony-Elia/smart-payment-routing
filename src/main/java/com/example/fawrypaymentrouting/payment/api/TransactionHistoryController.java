package com.example.fawrypaymentrouting.payment.api;

import com.example.fawrypaymentrouting.payment.dto.DailyTransactionHistoryResponseDto;
import com.example.fawrypaymentrouting.payment.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/billers")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService historyService;

    @GetMapping("/{billerId}/transactions")
    @ResponseStatus(HttpStatus.OK)
    public DailyTransactionHistoryResponseDto getHistory(
            @PathVariable UUID billerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {

        return historyService.getDailyHistory(billerId, date, pageable);
    }
}