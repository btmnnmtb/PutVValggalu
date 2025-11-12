package com.example.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddItemRequest {
    @NotNull
    private Integer cosmeticItemId;

    @Min(1)
    private Integer quantity = 1;
}