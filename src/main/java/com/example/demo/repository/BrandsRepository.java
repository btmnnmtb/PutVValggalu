package com.example.demo.repository;

import com.example.demo.model.Brands;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandsRepository extends JpaRepository<Brands, Integer> {

}
