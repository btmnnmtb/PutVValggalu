package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Data
public class CosmeticItemForm {
    private String imagePath;

    @NotBlank(message = "Название обязательно")
    @Size(max = 100, message = "Название до 100 символов")
    private String itemName;

    @NotNull(message = "Тип обязателен")
    private Integer cosmeticTypeId;

    @NotNull(message = "Бренд обязателен")
    private Integer brandId;

    @NotNull(message = "Производитель обязателен")
    private Integer manufacturerId;

    @NotNull(message = "Сертификат обязателен")
    private Integer certificateId;

    private Integer productStatusId;
    @NotBlank(message = "Описание обязателен")
    @Size(max = 100, message = "Описание до 100 символов")
    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.00", message = "Цена не может быть отрицательной")
    private BigDecimal price;

    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Кол-во не может быть отрицательным")
    @Max(value = 9999, message = "Слишком большое количество (макс: 9999)")
    private Integer quantity;

    private MultipartFile imageFile;

    @Column(name = "moderation_note", columnDefinition = "text")

    @Size(max = 100, message = "Отказ до 100 символов")
    private String moderationNote;





}