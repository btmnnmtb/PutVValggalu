package com.example.demo.repository;

import com.example.demo.model.Brands;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandsRepository extends JpaRepository<Brands, Integer> {
    Optional<Brands> findByBrandName(String brandName);


}
