package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final UsersRepository usersRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CosmeticItemRepository cosmeticItemRepository;

    @GetMapping
    public List<OrderDto> getOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderDto::fromEntity)
                .toList();
    }
    @GetMapping("/{login}/user")
    @Transactional(readOnly = true)
    public List<OrderDto> getOrders(@PathVariable String login) {
        User user = usersRepository.findByLogin(login).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return orderRepository.findAllByUser_LoginOrderByOrderDateDesc(login)
                .stream()
                .map(OrderDto::fromEntity)
                .toList();
    }
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Integer id) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден: id=" + id));
        return OrderDto.fromEntity(order);
    }
    @Operation(summary = "Созадание заказа" , description = "Создание заказа пользователем")
    @PostMapping
    public Orders createOrder(@RequestBody OrderDto dto) {
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId обязателен");
        }
        if (dto.getStatusId() == null) {
            throw new RuntimeException("statusId обязателен");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new RuntimeException("Список позиций пуст");
        }

        User user = usersRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: id=" + dto.getUserId()));
        Order_status status = orderStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new RuntimeException("Статус не найден: id=" + dto.getStatusId()));

        Orders order = Orders.builder()
                .user(user)
                .orderStatus(status)
                .orderDate(dto.getOrderDate() != null ? dto.getOrderDate() : new java.util.Date())
                .build();

        var lines = new ArrayList<Order_item>();
        for (OrderDto.OrderItemDto it : dto.getItems()) {
            if (it.getCosmeticItemId() == null) {
                throw new RuntimeException("cosmeticItemId обязателен в позиции");
            }

            Cosmetic_items product = cosmeticItemRepository.findById(it.getCosmeticItemId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: id=" + it.getCosmeticItemId()));

            Order_item line = Order_item.builder()
                    .order(order)
                    .cosmeticItem(product)
                    .quantity(it.getQuantity())
                    .price(it.getPrice() != null ? it.getPrice() : product.getPrice())
                    .build();

            lines.add(line);
        }

        order.setItems(lines);

        return orderRepository.save(order);
    }
    @Operation(summary = "Смена статуса заказ" , description = "Смена статуса заказа")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Integer id, @RequestBody OrderDto dto) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Заказ не найден: id=" + id));

        Order_status newStatus = orderStatusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Статус не найден: id=" + dto.getStatusId()));
        order.setOrderStatus(newStatus);
        order.setOrderDate(new java.util.Date());
        orderRepository.save(order);

        return ResponseEntity.ok(OrderDto.fromEntity(order));



    }
}