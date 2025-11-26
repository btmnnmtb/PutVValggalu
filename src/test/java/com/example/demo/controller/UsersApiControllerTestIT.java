package com.example.demo.controller;

import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.model.UserApiDto;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsersApiControllerTestIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    private Roles userRole;
    private User existingUser;

    @BeforeEach
    void setUp() {
        usersRepository.deleteAll();
        rolesRepository.deleteAll();

        userRole = new Roles();
        userRole.setRoleName("Пользователь");
        userRole = rolesRepository.save(userRole);

        existingUser = new User();
        existingUser.setLogin("TestUser");
        existingUser.setPasswordHash(passwordEncoder.encode("123456"));
        existingUser.setRole(userRole);
        existingUser = usersRepository.save(existingUser);
    }

    @Test
    void getAllUsers_returnsOkAndList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].login").value("TestUser"));
    }

    @Test
    void getUserByLogin_existing_returnsOk() throws Exception {
        mockMvc.perform(get("/api/users/{login}", "TestUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("TestUser"))
                .andExpect(jsonPath("$.role.roleName").value("Пользователь"));
    }

    @Test
    void getUserByLogin_notExisting_returnsServerError() throws Exception {

        mockMvc.perform(get("/api/users/{login}", "NoSuchUser"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createUser_validData_returnsOkAndUserCreated() throws Exception {
        UserApiDto dto = new UserApiDto();
        dto.setLogin("NewUser");
        dto.setPassword("123456");
        dto.setRoleName("Пользователь");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь создан"));

        var created = usersRepository.findByLogin("NewUser").orElse(null);
        assert created != null;
        assert created.getRole().getRoleName().equals("Пользователь");
    }

    @Test
    void updateUser_validData_returnsOkAndUserUpdated() throws Exception {
        UserApiDto dto = new UserApiDto();
        dto.setLogin("UpdatedUser");
        dto.setPassword("654321");
        dto.setRoleName("Пользователь");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/users/{id}", existingUser.getUserId())
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Пользователь обновлён"));

        var updated = usersRepository.findById(existingUser.getUserId()).orElseThrow();
        assert updated.getLogin().equals("UpdatedUser");
    }

    @Test
    void deleteUser_existing_returnsNoContentAndUserDeleted() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", existingUser.getUserId()))
                .andExpect(status().isNoContent());

        boolean exists = usersRepository.findById(existingUser.getUserId()).isPresent();
        assert !exists;
    }
}
