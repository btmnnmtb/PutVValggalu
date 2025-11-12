package com.example.demo.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommentsDto {

    @NotNull(message = "ID товара обязателен")
    private Integer cosmeticItemId;

    @NotNull(message = "ID пользователя обязателен")
    private Integer userId;

    @NotBlank(message = "Комментарий не может быть пустым")
    private String commentText;

    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Минимальный рейтинг — 1")
    @Max(value = 5, message = "Максимальный рейтинг — 5")
    private Integer rating;
}