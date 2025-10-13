package com.example.demo.repository;

import com.example.demo.model.CosmeticView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CosmeticViewRepository extends JpaRepository<CosmeticView, Integer> {
    List<CosmeticView> findByBrandName(String brandName);
    List<CosmeticView> findByManufacturerName(String manufacturerName);
    List<CosmeticView> findByStatusName(String statusName);
}
