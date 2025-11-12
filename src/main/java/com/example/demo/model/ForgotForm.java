package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotForm {
    @NotBlank(message = "Укажите логин")
    private String login;

    @NotBlank(message = "Укажите новый пароль")
    @Size(min = 6, max = 20, message = "Пароль 6–20 символов")
    private String newPassword;

    @NotBlank(message = "Подтвердите пароль")
    private String confirmPassword;
}