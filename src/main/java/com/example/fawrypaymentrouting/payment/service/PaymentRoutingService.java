package com.example.fawrypaymentrouting.payment.service;
import com.example.fawrypaymentrouting.biller.model.Biller;
import com.example.fawrypaymentrouting.biller.repository.BillerRepository;
import com.example.fawrypaymentrouting.gateway.model.Gateway;
import com.example.fawrypaymentrouting.gateway.repository.GatewayRepository;
import com.example.fawrypaymentrouting.payment.dto.*;
import com.example.fawrypaymentrouting.payment.model.Urgency;
import com.example.fawrypaymentrouting.payment.repository.PaymentTransactionRepository;
import com.example.fawrypaymentrouting.shared.exception.ResourceConflictException;
import com.example.fawrypaymentrouting.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRoutingService {
    private final GatewayRepository gatewayRepository;
    private final BillerRepository billerRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public PaymentRecommendationResponseDto recommendGateway(PaymentRecommendationRequestDto request) {
        log.info("Evaluating routing for Biller: {} Amount: {}", request.billerId(), request.amount());
        // 1. Fetch & Validate Biller
        Biller biller = billerRepository.findById(request.billerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));
        // 2. Validate Biller's Global Quota
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();

        BigDecimal billerDailyUsage = transactionRepository.getDailyUsageForBiller(biller.getId(), startOfDay, endOfDay);
        if (biller.getDailyQuota() != null && billerDailyUsage.add(request.amount()).compareTo(biller.getDailyQuota()) > 0) {
            throw new ResourceConflictException("Biller has exceeded global daily quota limit");
        }

        // 3. Fetch active gateways and filter
        List<Gateway> activeGateways = gatewayRepository.findByActiveTrue();

        List<Gateway> eligibleGateways = activeGateways.stream()
                .filter(g -> g.isValidAmount(request.amount())) // Fits amount constraints
                .filter(g -> isUrgencyMatch(g, request.urgency())) // Meets SLA
                .filter(g -> g.isAvailableAt(now)) // Currently awake
                .filter(g -> hasSufficientBillerQuotaOnGateway(biller.getId(), g, request.amount(), startOfDay, endOfDay)) // Biller hasn't hit gateway limits
                .sorted(Comparator.comparing(g -> g.calculateCommission(request.amount()))) // Order by cheapest
                .toList();

        if (eligibleGateways.isEmpty()) {
            throw new ResourceConflictException("No eligible gateways found for this request");
        }
        // 4. Construct Response (Top = Recommended, Rest = Alternatives)
        Gateway best = eligibleGateways.get(0);
        RecommendedGatewayDto bestDto = new RecommendedGatewayDto(
                best.getId(),
                best.getName(),
                best.calculateCommission(request.amount()),
                formatProcessingTime(best.getProcessingTimeInHours())
        );
        List<AlternativeGatewayDto> alternatives = eligibleGateways.stream()
                .skip(1)
                .map(g -> new AlternativeGatewayDto(g.getId(), g.calculateCommission(request.amount())))
                .collect(Collectors.toList());
        return new PaymentRecommendationResponseDto(bestDto, alternatives);
    }

    private boolean isUrgencyMatch(Gateway gateway, Urgency urgency) {
        if (urgency == Urgency.INSTANT) {
            return gateway.getProcessingTimeInHours().compareTo(BigDecimal.ZERO) == 0;
        }
        return true; // CAN_WAIT allows any timeframe
    }

    private boolean hasSufficientBillerQuotaOnGateway(UUID billerId, Gateway gateway, BigDecimal amount, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        BigDecimal billerDailyUsageOnGateway = transactionRepository.getDailyUsageForBillerOnGateway(billerId, gateway.getId(), startOfDay, endOfDay);
        return billerDailyUsageOnGateway.add(amount).compareTo(gateway.getDailyLimit()) <= 0;
    }

    private String formatProcessingTime(BigDecimal hours) {
        if (hours.compareTo(BigDecimal.ZERO) == 0) return "Instant";
        return hours.intValue() + " Hours";
    }
}
