package com.example.fawrypaymentrouting.payment.repository;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
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
}