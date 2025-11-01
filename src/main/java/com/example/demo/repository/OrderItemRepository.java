package com.example.demo.repository;

import com.example.demo.model.Order_item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<Order_item,Integer> {
}
