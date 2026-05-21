package com.example.fawrypaymentrouting.biller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BillerResponseDTO(
        UUID id,
        String name
) {}
