package com.example.demo.repository;

import com.example.demo.model.Cart_items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartsItemRepository extends JpaRepository<Cart_items, Integer> {
}
