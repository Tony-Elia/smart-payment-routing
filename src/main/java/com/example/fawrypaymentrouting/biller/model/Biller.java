package com.example.fawrypaymentrouting.biller.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "billers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Biller {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // Auto-generated UUID

    @Column(nullable = false)
    private String name;
}
