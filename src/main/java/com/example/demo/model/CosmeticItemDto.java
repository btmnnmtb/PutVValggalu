package com.example.demo.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CosmeticItemDto {

    private Integer cosmeticItemId;
    private String itemName;
    private Integer manufacturerId;
    private Integer brandId;
    private Integer cosmeticTypeId;
    private Integer certificateId;
    private Integer productStatusId;
    private String imagePath;
    private String description;
    private Integer quantity;
    private BigDecimal price;

    // “Читаемые” имена
    private String brandName;
    private String cosmeticTypeName;

    public static CosmeticItemDto fromEntity(Cosmetic_items e) {
        return CosmeticItemDto.builder()
                .cosmeticItemId(e.getCosmeticItemId())
                .itemName(e.getItemName())
                .manufacturerId(e.getManufacturerId())
                .brandId(e.getBrandId())
                .cosmeticTypeId(e.getCosmeticTypeId())
                .certificateId(e.getCertificateId())
                .productStatusId(e.getProductStatusId())
                .imagePath(e.getImagePath())
                .description(e.getDescription())
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .brandName(e.getBrand() != null ? e.getBrand().getBrandName() : null)
                .cosmeticTypeName(e.getCosmeticType() != null ? e.getCosmeticType().getCosmeticTypeName() : null)
                .build();
    }

    public Cosmetic_items toEntity() {
        return Cosmetic_items.builder()
                .cosmeticItemId(cosmeticItemId)
                .itemName(itemName)
                .manufacturerId(manufacturerId)
                .brandId(brandId)
                .cosmeticTypeId(cosmeticTypeId)
                .certificateId(certificateId)
                .productStatusId(productStatusId)
                .imagePath(imagePath)
                .description(description)
                .quantity(quantity)
                .price(price)
                .build();
    }
}