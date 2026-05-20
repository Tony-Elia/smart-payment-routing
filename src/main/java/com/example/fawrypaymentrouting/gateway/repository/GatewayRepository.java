package com.example.fawrypaymentrouting.gateway.repository;

import com.example.fawrypaymentrouting.gateway.model.Gateway;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GatewayRepository extends JpaRepository<Gateway, UUID> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Gateway> findByName(@NotBlank String name);
    List<Gateway> findByActiveTrue();
}
