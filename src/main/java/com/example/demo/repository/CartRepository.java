package com.example.demo.repository;

import com.example.demo.model.Carts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Carts, Integer> {
    Optional<Carts> findByUser_UserId(Integer userId);

}

