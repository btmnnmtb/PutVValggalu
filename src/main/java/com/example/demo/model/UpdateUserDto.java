package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @NotNull
    private Integer userId;

    @NotBlank(message = "Логин обязателен")
    @Size(min = 3, max = 20, message = "Логин должен быть от 3 до 20 символов")
    private String login;

    @Size(min = 6, max = 20, message = "Пароль должен быть от 6 до 20 символов")
    private String rawPassword;

    @NotBlank(message = "Роль обязательна")
    private String roleName;
}