package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Product_statuses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_status_id")
    private Integer productStatusId;

    @Column(name = "status_name", nullable = false, length = 100)
    private String statusName;
}
