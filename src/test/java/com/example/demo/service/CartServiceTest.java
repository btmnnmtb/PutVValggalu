package com.example.demo.service;

import com.example.demo.model.Cart_items;
import com.example.demo.model.Carts;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CartsItemRepository;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private CosmeticItemRepository cosmeticItemRepository;

    @Mock
    private CartsItemRepository cartsItemRepository;

    @InjectMocks
    private CartService cartService;


    private User createUser() {
        User u = new User();
        u.setUserId(1);
        return u;
    }

    private Carts createCart(User user) {
        Carts c = Carts.builder()
                .user(user)
                .build();
        c.setCartItems(new ArrayList<>());
        return c;
    }

    private Cosmetic_items createProduct(int id, int qty) {
        Cosmetic_items ci = new Cosmetic_items();
        ci.setCosmeticItemId(id);
        ci.setQuantity(qty);
        return ci;
    }

    private Cart_items createCartItem(int cartItemId, Carts cart, Cosmetic_items product, int qty) {
        Cart_items ci = new Cart_items();
        ci.setCartItemId(cartItemId);
        ci.setCarts(cart);
        ci.setCosmetic_items(product);
        ci.setQuantity(qty);
        return ci;
    }


    @Test
    void addCartUserNotFound() {
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addCart(1, "user", 1));

        assertEquals("Пользоватлеь не найден", ex.getMessage());
        verify(usersRepository).findByLogin("user");
        verifyNoMoreInteractions(cartRepository, cosmeticItemRepository, cartsItemRepository);
    }

    @Test
    void addCartItemNotFound() {
        User user = createUser();

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(createCart(user)));
        when(cosmeticItemRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addCart(1, "user", 1));

        assertEquals("товар не найден", ex.getMessage());
        verify(cosmeticItemRepository).findById(1);
    }

    @Test
    void addCartItemOutOfStock() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 0);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));
        when(cosmeticItemRepository.findById(1)).thenReturn(Optional.of(product));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.addCart(1, "user", 1));

        assertEquals("Товар закончился на складе", ex.getMessage());
    }

    @Test
    void addCartNewItem() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 10); // есть остаток

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));
        when(cosmeticItemRepository.findById(1)).thenReturn(Optional.of(product));

        cartService.addCart(1, "user", 2);

        assertEquals(1, cart.getCartItems().size());
        Cart_items item = cart.getCartItems().get(0);
        assertEquals(2, item.getQuantity());
        assertEquals(product, item.getCosmetic_items());

        // количество товара на складе уменьшилось
        assertEquals(8, product.getQuantity());

        verify(cartRepository).save(cart);
        verify(cosmeticItemRepository).save(product);
    }

    @Test
    void addCartExistingItemIncrementsQuantity() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 10);

        Cart_items existing = createCartItem(100, cart, product, 3);
        cart.getCartItems().add(existing);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));
        when(cosmeticItemRepository.findById(1)).thenReturn(Optional.of(product));

        cartService.addCart(1, "user", 2);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(5, existing.getQuantity());
        assertEquals(8, product.getQuantity());

        verify(cartsItemRepository).save(existing);
        verify(cosmeticItemRepository).save(product);
        verify(cartRepository, never()).save(cart);
    }



    @Test
    void getCartExisting() {
        User user = createUser();
        Carts cart = createCart(user);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        Carts result = cartService.getCart("user");

        assertSame(cart, result);
        verify(cartRepository, never()).save(any(Carts.class));
    }

    @Test
    void getCartCreatesNewIfNotExists() {
        User user = createUser();
        Carts newCart = createCart(user);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Carts.class))).thenReturn(newCart);

        Carts result = cartService.getCart("user");

        assertSame(newCart, result);
        verify(cartRepository).save(any(Carts.class));
    }


    @Test
    void removeAllOfItemUserNotFound() {
        when(usersRepository.findByLogin("user")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.removeAllOfItem("user", 10));

        assertEquals("Пользователь не найден", ex.getMessage());
    }

    @Test
    void removeAllOfItemCartNotFound() {
        User user = createUser();
        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.removeAllOfItem("user", 10));

        assertEquals("Коризна пуста", ex.getMessage());
    }

    @Test
    void removeAllOfItemNotFoundInCart() {
        User user = createUser();
        Carts cart = createCart(user);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.removeAllOfItem("user", 10));

        assertEquals("Товар не найден", ex.getMessage());
    }

    @Test
    void removeAllOfItemSuccess() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 5);
        Cart_items item = createCartItem(10, cart, product, 3);
        cart.getCartItems().add(item);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.removeAllOfItem("user", 10);

        assertTrue(cart.getCartItems().isEmpty());
        assertEquals(8, product.getQuantity()); // 5 + 3

        verify(cartsItemRepository).delete(item);
        verify(cosmeticItemRepository).save(product);
    }


    @Test
    void incrementOneNoStock() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 0);
        Cart_items item = createCartItem(10, cart, product, 2);
        cart.getCartItems().add(item);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.incrementOne("user", 10));

        assertEquals("Нет остатка на складе", ex.getMessage());
    }

    @Test
    void incrementOneSuccess() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 5);
        Cart_items item = createCartItem(10, cart, product, 2);
        cart.getCartItems().add(item);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.incrementOne("user", 10);

        assertEquals(3, item.getQuantity());
        assertEquals(4, product.getQuantity()); // 5 - 1

        verify(cartsItemRepository).save(item);
        verify(cosmeticItemRepository).save(product);
    }

    // ====== decrementOne ======

    @Test
    void decrementOneDecrementsIfMoreThanOne() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 5);
        Cart_items item = createCartItem(10, cart, product, 3);
        cart.getCartItems().add(item);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.decrementOne("user", 10);

        assertEquals(2, item.getQuantity());
        assertEquals(6, product.getQuantity()); // 5 + 1

        verify(cartsItemRepository).save(item);
        verify(cartsItemRepository, never()).delete(item);
        verify(cosmeticItemRepository).save(product);
    }

    @Test
    void decrementOneRemovesItemIfQuantityBecomesZero() {
        User user = createUser();
        Carts cart = createCart(user);
        Cosmetic_items product = createProduct(1, 5);
        Cart_items item = createCartItem(10, cart, product, 1);
        cart.getCartItems().add(item);

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.decrementOne("user", 10);

        assertTrue(cart.getCartItems().isEmpty());
        assertEquals(6, product.getQuantity()); // 5 + 1

        verify(cartsItemRepository).delete(item);
        verify(cosmeticItemRepository).save(product);
    }

    // ====== clearCart ======

    @Test
    void clearCartUserNotFound() {
        when(usersRepository.findByLogin("user")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.clearCart("user"));

        assertEquals("Пользователь не найден", ex.getMessage());
    }

    @Test
    void clearCartCartEmptyError() {
        User user = createUser();
        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cartService.clearCart("user"));

        assertEquals("Корзина пуста", ex.getMessage());
    }

    @Test
    void clearCartSuccess() {
        User user = createUser();
        Carts cart = createCart(user);

        Cosmetic_items p1 = createProduct(1, 5);
        Cosmetic_items p2 = createProduct(2, 10);

        Cart_items i1 = createCartItem(10, cart, p1, 2);
        Cart_items i2 = createCartItem(11, cart, p2, 3);

        cart.setCartItems(new ArrayList<>(List.of(i1, i2)));

        when(usersRepository.findByLogin("user")).thenReturn(Optional.of(user));
        when(cartRepository.findByUser_UserId(user.getUserId()))
                .thenReturn(Optional.of(cart));

        cartService.clearCart("user");

        assertTrue(cart.getCartItems().isEmpty());
        assertEquals(13, p2.getQuantity());

        verify(cartsItemRepository).deleteAll(anyList());
        verify(cartRepository).save(cart);
    }
}
