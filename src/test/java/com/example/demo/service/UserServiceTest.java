package com.example.demo.service;

import com.example.demo.Config.SecurityAuditor;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.management.relation.RoleInfoNotFoundException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UsersRepository usersRepository;
    @Mock RolesRepository roleRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditLogService auditLog;
    @Mock SecurityAuditor securityAuditor;
    @Mock ObjectMapper om;

    @InjectMocks
    UserService userService;
    @Test
    void loadUserByUsernameNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        UsernameNotFoundException ex =  assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("username"));
        assertEquals(username + " not found", ex.getMessage());
    }
    @Test
    void updatePasswordByLoginLenthPass() {
        String newPassword = "111";

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePasswordByLogin("login", newPassword)
        );
        assertEquals("Пароль должен быть от 6 до 20 символов", ex.getMessage());
    }

    @Test
    void createUserLoginNotNull() {
        String login = "";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(login , "1111" , "Пользователь")
        );
        assertEquals("Логин не может быть пустым" ,  ex.getMessage());
    }
    @Test
    void createUserPassNull() {
        String pass = "" ;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("login", pass , "role"));
        assertEquals("Пароль не может быть пустым" ,   ex.getMessage());
    }
    @Test
    void createUserUniqueLogin() {
        String login = "login";

        when(usersRepository.existsByLoginIgnoreCase(login))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(login, "1111", "role")
        );

        assertEquals("Такой логин уже существует", ex.getMessage());
    }
    @Test
    void createUserRoleNotFound() {
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser("login", "1111", "role"));
        assertEquals("Роль не найдена" , ex.getMessage());

    }

    @Test
    void updateUserFindUser() {
        when(usersRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.updateUser(100, "login", "pass", "role")
        );

        assertEquals("User not found", ex.getMessage());
    }
    @Test
    void updateUserLoginNull() {
        User user = new User();
        user.setUserId(11);


        when(usersRepository.findById(anyInt())).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(11, "", "pass", "Пользователь")
        );

        assertEquals("Login не может быть пустым", ex.getMessage());
    }
    @Test
    void updateUserRoleNotFound() {
        User user = new User();
        user.setUserId(11);
        user.setLogin("TestUser2");
        Roles oldRole = new Roles();
        oldRole.setRoleName("Пользователь");
        user.setRole(oldRole);
        when(usersRepository.findById(anyInt())).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(11, "login", "pass", "role")
        );
        assertEquals("Роль \"role\" не найдена", ex.getMessage());

    }

    @Test
    void deleteUserUserNotFound() {
        when(usersRepository.findById(anyInt()))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUser(11)
        );

        assertEquals("Пользователь не найден", ex.getMessage());
    }
}