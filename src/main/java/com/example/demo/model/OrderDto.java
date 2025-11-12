package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Integer orderId;
    private Integer userId;
    private Integer statusId;
    private Date orderDate;
    private BigDecimal total;
    private List<OrderItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Integer orderItemId;
        private Integer cosmeticItemId;
        private String  itemName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }
    public static OrderDto fromEntity(Orders o) {
        return OrderDto.builder()
                .orderId(o.getOrderId())
                .userId(o.getUser() != null ? o.getUser().getUserId() : null)
                .statusId(o.getOrderStatus() != null ? o.getOrderStatus().getOrdersStatusId() : null)
                .orderDate(o.getOrderDate())
                .total(o.getTotal())
                .items(o.getItems() != null ? o.getItems().stream()
                        .map(OrderDto::fromItemEntity)
                        .collect(Collectors.toList()) : null)
                .build();
    }

    private static OrderItemDto fromItemEntity(Order_item li) {
        BigDecimal lineTotal = li.getPrice() != null && li.getQuantity() != null
                ? li.getPrice().multiply(BigDecimal.valueOf(li.getQuantity()))
                : null;

        return OrderItemDto.builder()
                .orderItemId(li.getOrderItemId())
                .cosmeticItemId(li.getCosmeticItem() != null ? li.getCosmeticItem().getCosmeticItemId() : null)
                .itemName(li.getCosmeticItem() != null ? li.getCosmeticItem().getItemName() : null)
                .quantity(li.getQuantity())
                .price(li.getPrice())
                .lineTotal(lineTotal)
                .build();
    }
}
