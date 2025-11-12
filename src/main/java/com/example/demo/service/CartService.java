package com.example.demo.service;

import com.example.demo.model.Cart_items;
import com.example.demo.model.Carts;
import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.CartsItemRepository;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final CosmeticItemRepository cosmeticItemRepository;
    private final CartsItemRepository cartsItemRepository;

    @Transactional
    public void addCart(Integer cosmetic_item_id, String username , int quantity) {
        User user = usersRepository.findByLogin(username).orElseThrow(()-> new RuntimeException("Пользоватлеь не найден"));
        Carts carts = cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(()-> {
                    Carts Newcart = Carts.builder().user(user).build();
                    return cartRepository.save(Newcart);

                });
        Cosmetic_items cosmeticItems = cosmeticItemRepository.findById(cosmetic_item_id).orElseThrow(()-> new RuntimeException("товар не найден"));
        if (cosmeticItems.getQuantity() <= 0) {
            throw new RuntimeException("Товар закончился на складе");
        }

        Optional<Cart_items> cartItems = carts.getCartItems().stream()
                .filter(ci -> ci.getCosmetic_items().getCosmeticItemId().equals(cosmeticItems.getCosmeticItemId()))
                .findFirst();
        if (cartItems.isPresent()) {
            Cart_items item = cartItems.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartsItemRepository.save(item);
        }
        else {
            Cart_items newItem = Cart_items.builder().carts(carts).cosmetic_items(cosmeticItems).quantity(quantity).build();
            carts.getCartItems().add(newItem);
            cartRepository.save(carts);
        }
        cosmeticItems.setQuantity(cosmeticItems.getQuantity() - quantity);
        cosmeticItemRepository.save(cosmeticItems);
    }
    public Carts getCart(String username){
        User user = usersRepository.findByLogin(username).orElseThrow(()-> new RuntimeException("пользователь на найден"));
        return cartRepository.findByUser_UserId(user.getUserId()).orElseGet(()->{
            Carts newcarts = Carts.builder().user(user).build();
            return cartRepository.save(newcarts);
        });
    }
    public void removeAllOfItem(String username, Integer cartItemId) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Коризна пуста"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        int qty = item.getQuantity();
        var product = item.getCosmetic_items();

        cart.getCartItems().remove(item);
        cartsItemRepository.delete(item);

        product.setQuantity(product.getQuantity() + qty);
        cosmeticItemRepository.save(product);
    }
    @Transactional
    public void incrementOne(String username, Integer cartItemId) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        var product = item.getCosmetic_items();
        if (product.getQuantity() <= 0) {
            throw new RuntimeException("Нет остатка на складе");
        }
        item.setQuantity(item.getQuantity() + 1);
        cartsItemRepository.save(item);

        product.setQuantity(product.getQuantity() - 1);
        cosmeticItemRepository.save(product);

    }
    @Transactional
    public void decrementOne(String username, Integer cartItemId) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь на найден"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        var product = item.getCosmetic_items();
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartsItemRepository.save(item);
        } else {
            cart.getCartItems().remove(item);
            cartsItemRepository.delete(item);
        }
        product.setQuantity(product.getQuantity() + 1);
        cosmeticItemRepository.save(product);
    }
    @Transactional
    public void clearCart(String username) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Корзина пуста"));

        for (var item : cart.getCartItems()) {
            var product = item.getCosmetic_items();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            cosmeticItemRepository.save(product);
        }

        cartsItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }


}
