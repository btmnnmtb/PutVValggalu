package com.example.demo.service;

import com.example.demo.model.Order_item;
import com.example.demo.model.Orders;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UsersRepository usersRepository;
    private final CartRepository cartRepository;
    private final CartsItemRepository cartsItemRepository;
    private final OrderRepository ordersRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Transactional
    public Integer checkout(String username) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        var status = orderStatusRepository.findByOrderStatus("Собираеться")
                .orElseThrow(() -> new RuntimeException("Order status 'Собираеться' not found"));

        var order = Orders.builder()
                .user(user)
                .orderDate(new Date())
                .orderStatus(status)
                .total(BigDecimal.ZERO)
                .build();

        var items = new ArrayList<Order_item>();
        BigDecimal total = BigDecimal.ZERO;

        for (var ci : cart.getCartItems()) {
            var price = ci.getCosmetic_items().getPrice();
            var qty = BigDecimal.valueOf(ci.getQuantity());

            var oi = Order_item.builder()
                    .order(order)
                    .cosmeticItem(ci.getCosmetic_items())
                    .price(price)
                    .quantity(ci.getQuantity())
                    .build();

            items.add(oi);
            total = total.add(price.multiply(qty));
        }

        order.setItems(items);
        order.setTotal(total);

        ordersRepository.save(order);

        cartsItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return order.getOrderId();
    }
}
