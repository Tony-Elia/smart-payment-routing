package com.example.fawrypaymentrouting.payment.api;

import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionResponseDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationResponseDto;
import com.example.fawrypaymentrouting.payment.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService transactionService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<PaymentTransactionResponseDto> getAllTransactions(Pageable pageable) {
        return transactionService.findAll(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentTransactionResponseDto createTransaction(@Valid @RequestBody PaymentTransactionRequestDto request) {
        return transactionService.create(request);
    }

    @PostMapping("/recommend")
    @ResponseStatus(HttpStatus.OK)
    public PaymentRecommendationResponseDto recommendGateway(@Valid @RequestBody PaymentRecommendationRequestDto request) {
        return transactionService.recommendGateway(request);
    }
}
