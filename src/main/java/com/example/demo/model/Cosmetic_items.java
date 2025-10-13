package com.example.demo.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "cosmetic_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Cosmetic_items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cosmetic_item_id")
    private Integer cosmeticItemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    // FK как простые поля
    @Column(name = "manufacturer_id")
    private Integer manufacturerId;

    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "cosmetic_type_id")
    private Integer cosmeticTypeId;

    @Column(name = "certificate_id")
    private Integer certificateId;

    @Column(name = "product_status_id")
    private Integer productStatusId;

    @Column(name = "image_path", length = 255)
    private String imagePath;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
}