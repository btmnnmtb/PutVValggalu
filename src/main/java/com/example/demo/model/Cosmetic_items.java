package com.example.demo.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "manufacturer_id")
    private Integer manufacturerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brands brand;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cosmetic_type_id", insertable = false, updatable = false)
    private Cosmetic_types cosmeticType;

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

    @OneToMany(mappedBy = "cosmeticItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favourites> favourites = new ArrayList<>();

    @OneToMany(mappedBy = "cosmeticItem", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

}
