package com.example.demo.repository;

import com.example.demo.model.Product_statuses;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductStatusRepository extends JpaRepository <Product_statuses, Integer> {
    Optional<Product_statuses> findByStatusName(String statusName);
}
