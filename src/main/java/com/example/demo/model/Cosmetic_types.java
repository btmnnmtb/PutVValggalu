package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "cosmetic_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Cosmetic_types {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cosmetic_type_id")
    private Integer cosmeticTypeId;

    @Column(name = "cosmetic_type_name", nullable = false, length = 100)
    private String cosmeticTypeName;

    @Column(name="cosmetic_type_price", precision = 10, scale = 2)
    private BigDecimal cosmeticTypePrice;
}
