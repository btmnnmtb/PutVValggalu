package com.example.demo.service;

import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UsersRepository usersRepository;

    @Mock
    RolesRepository rolesRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    void registerUserRoleFound() {
        String roleName = "ROLE_USER";
        when(rolesRepository.findByRoleName(roleName)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.registerUser("dddddd", "111111", roleName));

        assertEquals("Роль не найдена: " + roleName, ex.getMessage());
    }
}