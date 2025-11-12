package com.example.demo.controller;

import com.example.demo.model.UpdateUserDto;
import com.example.demo.model.UserApiDto;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersApiController {
    private final UsersRepository usersRepository;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Вывести всех клиентов" , description = "Вывод всех клиентов из базы данных")
    public List<User> getAllUsers() { return usersRepository.findAll(); }

    @GetMapping("/{login}")
    @Operation(summary = "Вывести клиента по логину" , description = "Вывод клиента по логину из базы данных")
    public User getUserByLogin(@PathVariable String login) {
        return usersRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользоватлеь не найден"));
    }
    @PostMapping
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserApiDto dto) {
        userService.createUser(dto.getLogin(), dto.getPassword(), dto.getRoleName());
        return ResponseEntity.ok("Пользователь создан");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @Valid @RequestBody UserApiDto dto) {
        userService.updateUser(id, dto.getLogin(), dto.getPassword(), dto.getRoleName());
        return ResponseEntity.ok("Пользователь обновлён");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}