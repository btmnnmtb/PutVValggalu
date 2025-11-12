package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsersRepository extends JpaRepository <User, Integer> {

    @EntityGraph(attributePaths = "role")
    Optional<User> findByLogin(String login);
    boolean existsByLoginIgnoreCase(String login);

    boolean existsByLoginIgnoreCaseAndUserIdNot(String login, Integer userId);

}
