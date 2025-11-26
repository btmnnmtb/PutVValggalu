package com.example.demo.repository;

import com.example.demo.model.Quality_certificates;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QualityCertificRepository extends JpaRepository<Quality_certificates, Integer> {
    Optional<Quality_certificates> findByCertificateName(String certificateName);

}
