package com.example.demo.repository;

import com.example.demo.model.Order_status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderStatusRepository extends JpaRepository<Order_status,Integer> {
    Optional<Order_status> findByOrderStatus(String orderStatus);
}
