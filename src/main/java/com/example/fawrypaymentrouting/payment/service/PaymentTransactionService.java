package com.example.fawrypaymentrouting.payment.service;

import com.example.fawrypaymentrouting.biller.model.Biller;
import com.example.fawrypaymentrouting.biller.repository.BillerRepository;
import com.example.fawrypaymentrouting.gateway.model.Gateway;
import com.example.fawrypaymentrouting.gateway.repository.GatewayRepository;
import com.example.fawrypaymentrouting.payment.dto.*;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    // ====================================================================================
    // DEPENDENCIES
    // ====================================================================================
    private final PaymentTransactionRepository transactionRepository;
    private final BillerRepository billerRepository;
    private final GatewayRepository gatewayRepository;
    private final PaymentTransactionMapper transactionMapper;

    // ====================================================================================
    // PUBLIC API METHODS
    // ====================================================================================

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

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();

        if (!gateway.isActive()) {
            throw new ResourceConflictException("Gateway is inactive.");
        }

        if (!gateway.isAvailableAt(now)) {
            throw new ResourceConflictException("Gateway is currently out of operating hours.");
        }

        if (!isUrgencyMatch(gateway, request.urgency())) {
            throw new ResourceConflictException("Gateway does not support the requested urgency level.");
        }

        if (!gateway.isValidAmount(request.amount())) {
            throw new ResourceConflictException("Amount is strictly outside minimum/maximum gateway limits.");
        }

        BigDecimal dailyUsage = transactionRepository.getDailyUsageForGatewayAndBiller(
                biller.getId(),
                gateway.getId(),
                startOfDay,
                endOfDay
        );
        if (dailyUsage.add(request.amount()).compareTo(gateway.getDailyLimit()) > 0) {
            throw new ResourceConflictException("Biller has reached the daily limit with this gateway.");
        }

        PaymentTransaction transaction = transactionMapper.toEntity(request);
        transaction.setBiller(biller);
        transaction.setGateway(gateway);

        BigDecimal commission = gateway.calculateCommission(request.amount());
        transaction.setCommissionApplied(commission);
        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionMapper.toDto(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public PaymentRecommendationResponseDto recommendGateway(PaymentRecommendationRequestDto request) {
        log.info("Evaluating standard routing for Biller: {} Amount: {}", request.billerId(), request.amount());

        Map<UUID, BigDecimal> usageMap = getDailyUsageMap(request.billerId());

        List<Gateway> eligibleGateways = findEligibleGateways(
                request,
                g -> g.isValidAmount(request.amount()), // Strict amount check
                g -> hasSufficientBillerQuotaOnGateway(usageMap, g, request.amount()), // Strict quota check
                Comparator.comparing((Gateway g) -> g.calculateCommission(request.amount()))
                        .thenComparing(Gateway::getProcessingTimeInHours) // Cost -> Speed
        );

        if (eligibleGateways.isEmpty()) {
            throw new ValidationException("No eligible routing paths available for this transaction.");
        }

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
                .toList();

        return new PaymentRecommendationResponseDto(bestDto, alternatives);
    }

    @Transactional(readOnly = true)
    public PaymentSplitResponseDto recommendSplit(PaymentRecommendationRequestDto request) {
        log.info("Evaluating split routing for Biller: {} Amount: {}", request.billerId(), request.amount());

        Map<UUID, BigDecimal> usageMap = getDailyUsageMap(request.billerId());

        List<Gateway> eligibleGateways = findEligibleGateways(
                request,
                g -> g.meetsMinLimit(request.amount()), // Only enforce minimum limit
                g -> true, // Dynamic Quota: We do not filter by qouta availability for split routing
                Comparator.comparing((Gateway g) -> calculateProjectedSplitCommission(request.amount(), g))
                        .thenComparing(Gateway::getProcessingTimeInHours) // calc split commissions in O(1) & speed
        );

        if (eligibleGateways.isEmpty()) {
            throw new ResourceConflictException("No gateways available capable of processing this request.");
        }

        Gateway bestGateway = eligibleGateways.get(0);

        boolean requiresSplitting = bestGateway.getMaxTransaction() != null && request.amount().compareTo(bestGateway.getMaxTransaction()) > 0;

        List<BigDecimal> splits = requiresSplitting
                ? generateEqualSplits(request.amount(), bestGateway)
                : List.of(request.amount());

        // Accurate commission calculation for all split chunks combined
        BigDecimal totalCommission = splits.stream()
                .map(bestGateway::calculateCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentSplitResponseDto(
                bestGateway.getName(),
                bestGateway.getId(),
                requiresSplitting,
                splits,
                totalCommission,
                hasSufficientBillerQuotaOnGateway(usageMap, bestGateway, request.amount()),
                splits.size()
        );
    }

    // ====================================================================================
    // PRIVATE CORE ENGINE HELPERS
    // ====================================================================================

    private List<Gateway> findEligibleGateways(
            PaymentRecommendationRequestDto request,
            Predicate<Gateway> amountFilter,
            Predicate<Gateway> quotaFilter,
            Comparator<Gateway> sorter) {

        LocalDateTime now = LocalDateTime.now();

        return gatewayRepository.findByActiveTrue().stream()
                .filter(g -> isUrgencyMatch(g, request.urgency()))
                .filter(g -> g.isAvailableAt(now))
                .filter(amountFilter)
                .filter(quotaFilter)
                .sorted(sorter)
                .toList();
    }

    private List<BigDecimal> generateEqualSplits(BigDecimal amount, Gateway gateway) {
        BigDecimal mx = gateway.getMaxTransaction();
        BigDecimal mn = gateway.getMinTransaction();

        if (mx == null || amount.compareTo(mx) <= 0) {
            if (amount.compareTo(mn) < 0) {
                throw new ResourceConflictException("Amount is below the minimum limit.");
            }
            return List.of(amount);
        }

        int slices = amount.divide(mx, 0, RoundingMode.CEILING).intValue();
        BigDecimal sliceCount = BigDecimal.valueOf(slices);

        BigDecimal averageSlice = amount.divide(sliceCount, 2, RoundingMode.HALF_UP);
        if (averageSlice.compareTo(mn) < 0) {
            throw new ResourceConflictException("Cannot split without violating minimum limits.");
        }

        BigDecimal baseSlice = amount.divide(sliceCount, 2, RoundingMode.DOWN);
        BigDecimal totalOfBaseSlices = baseSlice.multiply(BigDecimal.valueOf(slices - 1));
        BigDecimal finalSlice = amount.subtract(totalOfBaseSlices);

        List<BigDecimal> splits = new ArrayList<>(slices);
        for (int i = 0; i < slices - 1; i++) {
            splits.add(baseSlice);
        }
        splits.add(finalSlice);

        return splits;
    }

    // ====================================================================================
    // PRIVATE UTILITY HELPERS
    // ====================================================================================

    private Map<UUID, BigDecimal> getDailyUsageMap(UUID billerId) {
        // Validation check inside the helper ensures the biller actually exists
        Biller biller = billerRepository.findById(billerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biller not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();

        return transactionRepository.getDailyUsagePerGatewayForBiller(biller.getId(), startOfDay, endOfDay)
                .stream()
                .collect(Collectors.toMap(
                        PaymentTransactionRepository.GatewayUsage::getGatewayId,
                        PaymentTransactionRepository.GatewayUsage::getUsage
                ));
    }

    private boolean isUrgencyMatch(Gateway gateway, Urgency urgency) {
        if (urgency == Urgency.INSTANT) {
            return gateway.getProcessingTimeInHours().compareTo(BigDecimal.ZERO) == 0;
        }
        return true;
    }

    private boolean hasSufficientBillerQuotaOnGateway(Map<UUID, BigDecimal> gatewayUsageMap, Gateway gateway, BigDecimal amount) {
        BigDecimal currentUsage = gatewayUsageMap.getOrDefault(gateway.getId(), BigDecimal.ZERO);
        return currentUsage.add(amount).compareTo(gateway.getDailyLimit()) <= 0;
    }

    private String formatProcessingTime(BigDecimal hours) {
        if (hours.compareTo(BigDecimal.ZERO) == 0) return "Instant";
        return hours.intValue() + " Hours";
    }

    private BigDecimal calculateProjectedSplitCommission(BigDecimal amount, Gateway gateway) {
        BigDecimal mx = gateway.getMaxTransaction();
        BigDecimal mn = gateway.getMinTransaction();

        if (mx == null || amount.compareTo(mx) <= 0) {
            if (amount.compareTo(mn) < 0) {
                return BigDecimal.valueOf(Double.MAX_VALUE);
            }
            return gateway.calculateCommission(amount);
        }

        int slices = amount.divide(mx, 0, RoundingMode.CEILING).intValue();
        BigDecimal sliceCount = BigDecimal.valueOf(slices);

        BigDecimal averageSlice = amount.divide(sliceCount, 2, RoundingMode.HALF_UP);
        if (averageSlice.compareTo(mn) < 0) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }

        BigDecimal normalCommission = gateway.calculateCommission(amount);
        BigDecimal extraFixedFees = gateway.getCommissionFixed().multiply(BigDecimal.valueOf(slices - 1));

        return normalCommission.add(extraFixedFees);
    }
}