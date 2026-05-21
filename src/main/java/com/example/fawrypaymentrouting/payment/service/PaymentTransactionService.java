package com.example.fawrypaymentrouting.payment.service;

import com.example.fawrypaymentrouting.biller.model.Biller;
import com.example.fawrypaymentrouting.biller.repository.BillerRepository;
import com.example.fawrypaymentrouting.gateway.model.Gateway;
import com.example.fawrypaymentrouting.gateway.repository.GatewayRepository;
import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentTransactionResponseDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationRequestDto;
import com.example.fawrypaymentrouting.payment.dto.PaymentRecommendationResponseDto;
import com.example.fawrypaymentrouting.payment.dto.RecommendedGatewayDto;
import com.example.fawrypaymentrouting.payment.dto.AlternativeGatewayDto;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
import com.example.fawrypaymentrouting.payment.model.TransactionStatus;
import com.example.fawrypaymentrouting.payment.model.Urgency;
import com.example.fawrypaymentrouting.payment.repository.PaymentTransactionRepository;
import com.example.fawrypaymentrouting.shared.exception.ResourceConflictException;
import com.example.fawrypaymentrouting.shared.exception.ResourceNotFoundException;
import com.example.fawrypaymentrouting.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository transactionRepository;
    private final BillerRepository billerRepository;
    private final GatewayRepository gatewayRepository;
    private final PaymentTransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public Page<PaymentTransactionResponseDto> findAll(Pageable pageable) {
        return transactionRepository.findAll(pageable)
                .map(transactionMapper::toDto);
    }

    @Transactional
    public PaymentTransactionResponseDto create(PaymentTransactionRequestDto request) {
        Biller biller = billerRepository.findById(request.billerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));

        Gateway gateway = gatewayRepository.findById(request.gatewayId())
                .orElseThrow(() -> new ResourceNotFoundException("Gateway not found"));

        PaymentTransaction transaction = transactionMapper.toEntity(request);
        transaction.setBiller(biller);
        transaction.setGateway(gateway);

        // Compute commission naturally from the gateway rules
        BigDecimal commission = gateway.calculateCommission(request.amount());
        transaction.setCommissionApplied(commission);

        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public PaymentRecommendationResponseDto recommendGateway(PaymentRecommendationRequestDto request) {
        log.info("Evaluating routing for Biller: {} Amount: {}", request.billerId(), request.amount());
        // 1. Fetch & Validate Biller
        Biller biller = billerRepository.findById(request.billerId())
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));
        // 2. Validate Biller's Global Quota
        LocalDateTime now = LocalDateTime.now();
        log.info("Current time (adjusted): {}", now);
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();

        // 3. Pre-fetch Biller usage per Gateway (eliminates N+1 query problem)
        Map<UUID, BigDecimal> gatewayUsageMap = transactionRepository.getDailyUsagePerGatewayForBiller(biller.getId(), startOfDay, endOfDay)
                .stream()
                .collect(Collectors.toMap(PaymentTransactionRepository.GatewayUsage::getGatewayId, PaymentTransactionRepository.GatewayUsage::getUsage));
        // log the usage map
        log.info("Usage Map: {}", gatewayUsageMap);
        // 4. Fetch active gateways and filter
        List<Gateway> activeGateways = gatewayRepository.findByActiveTrue();

        List<Gateway> eligibleGateways = activeGateways.stream()
                .filter(g -> g.isValidAmount(request.amount())) // Fits amount constraints
                .filter(g -> isUrgencyMatch(g, request.urgency())) // Meets SLA
                .filter(g -> g.isAvailableAt(now)) // Valid against operating hours
                .filter(g -> hasSufficientBillerQuotaOnGateway(gatewayUsageMap, g, request.amount()))
                .sorted(Comparator.comparing((Gateway g) -> g.calculateCommission(request.amount()))
                                  .thenComparing(Gateway::getProcessingTimeInHours)) // Added secondary sort
                .toList();

        if (eligibleGateways.isEmpty()) {
            throw new ValidationException("No eligible routing paths available for this transaction.");
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
                .map(g -> new AlternativeGatewayDto(g.getId(), g.getName(), g.calculateCommission(request.amount())))
                .collect(Collectors.toList());
        return new PaymentRecommendationResponseDto(bestDto, alternatives);
    }

    private boolean isUrgencyMatch(Gateway gateway, Urgency urgency) {
        if (urgency == Urgency.INSTANT) {
            return gateway.getProcessingTimeInHours().compareTo(BigDecimal.ZERO) == 0;
        }
        return true; // CAN_WAIT allows any timeframe
    }

    private boolean hasSufficientBillerQuotaOnGateway(Map<UUID, BigDecimal> gatewayUsageMap, Gateway gateway, BigDecimal amount) {
        BigDecimal currentUsage = gatewayUsageMap.getOrDefault(gateway.getId(), BigDecimal.ZERO);
        return currentUsage.add(amount).compareTo(gateway.getDailyLimit()) <= 0;
    }

    private String formatProcessingTime(BigDecimal hours) {
        if (hours.compareTo(BigDecimal.ZERO) == 0) return "Instant";
        return hours.intValue() + " Hours";
    }
}
