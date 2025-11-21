package com.example.demo.service;

import com.example.demo.model.Cart_items;
import com.example.demo.model.Carts;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.OrderStatusRepository;
import com.example.demo.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    OrderRepository orderRepository;
    @Mock
    UsersRepository usersRepository;
    @Mock
    CartRepository cartRepository;
    @Mock
    OrderStatusRepository orderStatusRepository;
    @InjectMocks
    private OrderService orderService;

    @Test
    void checkoutUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(username)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(username));

        assertEquals("User not found", ex.getMessage());
    }
    @Test
    void checkoutCartNotFound() {
        String username = "username";
        User user = new User();
        user.setUserId(1);

        when(usersRepository.findByLogin(username)).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(username));
        assertEquals("Cart not found", ex.getMessage());
    }
    @Test
    void checkoutCartEmpty() {
        String username = "username";

        User user = new User();
        user.setUserId(1);

        when(usersRepository.findByLogin(username))
                .thenReturn(Optional.of(user));


        Carts cart = new Carts();
        cart.setUser(user);

        cart.setCartItems(Collections.emptyList());

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));


        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(username));

        assertEquals("Cart is empty", ex.getMessage());
    }
    @Test
    void checkoutStatusNotFound() {
        String username = "username";
        User user = new User();
        user.setUserId(1);
        when(usersRepository.findByLogin(username)).thenReturn(Optional.of(user));

        Carts cart = new Carts();
        cart.setUser(user);

        Cart_items item = new Cart_items();
        item.setQuantity(1);

        Cosmetic_items product = new Cosmetic_items();
        product.setPrice(BigDecimal.TEN);
        item.setCosmetic_items(product);

        cart.setCartItems(List.of(item));

        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));


        when(orderStatusRepository.findByOrderStatus(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.checkout(username));
        assertEquals("Order status 'Собираеться' not found", ex.getMessage());



    }
}