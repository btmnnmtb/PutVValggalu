package com.example.demo.model;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "cosmeticItemId")

public class Cosmetic_items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cosmetic_item_id")
    private Integer cosmeticItemId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "manufacturer_id")
    private Integer manufacturerId;

    @Column(name = "brand_id")
    private Integer brandId;

    @Column(name = "cosmetic_type_id")
    private Integer cosmeticTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brands brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
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
    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Минимальное количество — 0")
    @Max(value = 9999, message = "Слишком большое количество (макс: 9999)")
    private Integer quantity;

    @Column(name = "price", precision = 10, scale = 2)
    @NotNull(message = "Цена обязательна")
    private BigDecimal price;

    @Builder.Default
    @OneToMany(mappedBy = "cosmeticItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favourites> favourites = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "cosmeticItem", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comments> comments = new ArrayList<>();

    @Column(name = "moderation_note", columnDefinition = "text")
    private String moderationNote;
}
