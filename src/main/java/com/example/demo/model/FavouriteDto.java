package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteDto {
    private Integer favouriteId;
    private Integer userId;
    private Integer cosmeticItemId;
    private String itemName;
    private String brandName;
    private String imagePath;

    public static FavouriteDto fromEntity(Favourites f) {
        return FavouriteDto.builder()
                .favouriteId(f.getFavouriteId())
                .userId(f.getUser() != null ? f.getUser().getUserId() : null)
                .cosmeticItemId(f.getCosmeticItem() != null ? f.getCosmeticItem().getCosmeticItemId() : null)
                .itemName(f.getCosmeticItem() != null ? f.getCosmeticItem().getItemName() : null)
                .brandName(f.getCosmeticItem() != null && f.getCosmeticItem().getBrand() != null
                        ? f.getCosmeticItem().getBrand().getBrandName()
                        : null)
                .imagePath(f.getCosmeticItem() != null ? f.getCosmeticItem().getImagePath() : null)
                .build();
    }
}
