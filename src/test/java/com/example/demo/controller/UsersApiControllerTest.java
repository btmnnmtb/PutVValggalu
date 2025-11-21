package com.example.demo.controller;

import com.example.demo.Config.SecurityAuditor;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class UsersApiControllerTest {
    @Mock
    UsersRepository usersRepository;

    @Mock
    RolesRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ObjectMapper om;

    @Mock
    SecurityAuditor securityAuditor;

    @Mock
    AuditLogService auditLog;

    @InjectMocks
    UserService userService;

    @Test
    void createUserTest() {

    }

    @Test
    void updateUserTest() {
        assertNotNull(usersRepository);
        assertNotNull(userService);
        when(usersRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.updateUser(100, "login", "pass", "role")
        );

        assertEquals("User not found", ex.getMessage());
    }
}