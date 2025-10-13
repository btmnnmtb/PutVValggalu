package com.example.demo.repository;

import com.example.demo.model.Cosmetic_items;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CosmeticItemRepository extends JpaRepository<Cosmetic_items, Integer> {
}
