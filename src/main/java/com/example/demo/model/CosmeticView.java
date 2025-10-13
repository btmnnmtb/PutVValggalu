package com.example.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "cosmetic_table")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CosmeticView {

    @Id
    @Column(name = "cosmetic_item_id")
    private Integer cosmeticItemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "description")
    private String description;

    @Column(name = "manufacturer_name")
    private String manufacturerName;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "cosmetic_type_name")
    private String cosmeticTypeName;

    @Column(name = "certificate_name")
    private String certificateName;

    @Column(name = "status_name")
    private String statusName;
}
