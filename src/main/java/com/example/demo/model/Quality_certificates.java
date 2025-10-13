package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Entity
@Table(name = "quality_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Quality_certificates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="certificate_id")
    private Integer certificateId;

    @Column(name = "certificate_name", nullable = false, length = 100)
    private String certificateName;

    @Column(name = "certificate_price", precision = 10 , scale = 2)
    private BigDecimal certificatePrice;
}
