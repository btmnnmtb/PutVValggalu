package com.example.demo.repository;

import com.example.demo.model.Manufacturers;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManufactureRepository extends JpaRepository<Manufacturers, Integer> {
}

