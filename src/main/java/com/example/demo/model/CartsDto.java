package com.example.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartsDto {
    private Integer cartId;
    private Integer userId;
    private String username;
    private BigDecimal total;
    private List<CartItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDto {
        private Integer cartItemId;
        private Integer cosmeticItemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }

    public static CartsDto fromEntity(Carts cart) {
        return CartsDto.builder()
                .cartId(cart.getCart_id())
                .userId(cart.getUser() != null ? cart.getUser().getUserId() : null)
                .username(cart.getUser() != null ? cart.getUser().getLogin() : null)
                // у тебя в сущности Carts.total сейчас Double — конвертируем безопасно:
                .total(cart.getTotal() != null ? BigDecimal.valueOf(cart.getTotal()) : null)
                .items(cart.getCartItems() != null
                        ? cart.getCartItems().stream().map(CartsDto::fromItemEntity).toList()
                        : Collections.emptyList())
                .build();
    }

    private static CartItemDto fromItemEntity(Cart_items ci) {
        BigDecimal price = (ci.getCosmetic_items() != null) ? ci.getCosmetic_items().getPrice() : null;

        Integer qty = ci.getQuantity();
        BigDecimal lineTotal = (price != null && qty != null)
                ? price.multiply(BigDecimal.valueOf(qty))
                : null;

        return CartItemDto.builder()
                .cartItemId(ci.getCartItemId())
                .cosmeticItemId(ci.getCosmetic_items() != null ? ci.getCosmetic_items().getCosmeticItemId() : null)
                .itemName(ci.getCosmetic_items() != null ? ci.getCosmetic_items().getItemName() : null)
                .quantity(qty)
                .price(price)
                .lineTotal(lineTotal)
                .build();
    }

}