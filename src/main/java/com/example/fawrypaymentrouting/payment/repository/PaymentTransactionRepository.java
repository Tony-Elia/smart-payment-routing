package com.example.fawrypaymentrouting.payment.repository;
import com.example.fawrypaymentrouting.payment.dto.GatewaySummaryDto;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    interface GatewayUsage {
        UUID getGatewayId();
        BigDecimal getUsage();
    }

    @Query("SELECT t.gateway.id AS gatewayId, COALESCE(SUM(t.amount), 0) AS usage FROM PaymentTransaction t WHERE t.biller.id = :billerId AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay GROUP BY t.gateway.id")
    List<GatewayUsage> getDailyUsagePerGatewayForBiller(@Param("billerId") UUID billerId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    boolean existsByGatewayId(UUID id);

    Page<PaymentTransaction> findAllByBillerIdAndCreatedAtBetween(
            UUID billerId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay,
            Pageable pageable
    );

    @Query("""
        SELECT new com.example.fawrypaymentrouting.payment.dto.GatewaySummaryDto(
            t.gateway.name, 
            COUNT(t), 
            COALESCE(SUM(t.amount), 0), 
            COALESCE(SUM(t.commissionApplied), 0)
        )
        FROM PaymentTransaction t
        WHERE t.biller.id = :billerId
          AND t.createdAt BETWEEN :start AND :end
        GROUP BY t.gateway.name
    """)
    List<GatewaySummaryDto> getDailyGatewaySummaries(
            @Param("billerId") UUID billerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}