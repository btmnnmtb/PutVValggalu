package com.example.demo.repository;

import com.example.demo.model.Actions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.*;
import java.util.Optional;

public interface ActionsRepository extends JpaRepository<Actions, Integer> {
    @Query("SELECT a FROM Actions a WHERE a.actionName = :name")
    Optional<Actions> findByActionName(@Param("name") String name);
}
