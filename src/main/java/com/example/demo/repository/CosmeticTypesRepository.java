package com.example.demo.repository;

import com.example.demo.model.Cosmetic_types;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CosmeticTypesRepository extends JpaRepository<Cosmetic_types, Integer>{
    Optional<Cosmetic_types> findByCosmeticTypeName(String cosmeticTypeName);

}
