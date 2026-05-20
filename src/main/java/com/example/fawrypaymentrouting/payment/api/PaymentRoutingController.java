package com.example.fawrypaymentrouting.payment.api;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationResponseDto;
import com.example.fawrypaymentrouting.payment.service.PaymentRoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRoutingController {
    private final PaymentRoutingService paymentRoutingService;

    @PostMapping("/recommend")
    @ResponseStatus(HttpStatus.OK)
    public PaymentRecommendationResponseDto recommendGateway(@Valid @RequestBody PaymentRecommendationRequestDto request) {
        return paymentRoutingService.recommendGateway(request);
    }
}
