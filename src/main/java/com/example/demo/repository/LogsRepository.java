package com.example.demo.repository;

import com.example.demo.model.Logs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogsRepository extends JpaRepository<Logs, Integer> {}

