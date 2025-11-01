package com.example.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders_status")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order_status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orders_status_id")
    private Integer ordersStatusId;

    @Column(name = "order_status")
    private String orderStatus;
}
