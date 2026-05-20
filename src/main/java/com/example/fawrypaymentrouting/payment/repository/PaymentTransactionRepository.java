package com.example.fawrypaymentrouting.payment.repository;
import com.example.fawrypaymentrouting.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.biller.id = :billerId AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    BigDecimal getDailyUsageForBiller(@Param("billerId") UUID billerId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM PaymentTransaction t WHERE t.biller.id = :billerId AND t.gateway.id = :gatewayId AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    BigDecimal getDailyUsageForBillerOnGateway(@Param("billerId") UUID billerId, @Param("gatewayId") UUID gatewayId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
