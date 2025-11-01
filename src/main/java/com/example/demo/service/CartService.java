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
        User user = usersRepository.findByLogin(username).orElseThrow(()-> new RuntimeException("user not found"));
        Carts carts = cartRepository.findByUser_UserId(user.getUserId())
                .orElseGet(()-> {
                    Carts Newcart = Carts.builder().user(user).build();
                    return cartRepository.save(Newcart);

                });
        Cosmetic_items cosmeticItems = cosmeticItemRepository.findById(cosmetic_item_id).orElseThrow(()-> new RuntimeException("cosmetic item not found"));
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
        User user = usersRepository.findByLogin(username).orElseThrow(()-> new RuntimeException("user not found"));
        return cartRepository.findByUser_UserId(user.getUserId()).orElseGet(()->{
            Carts newcarts = Carts.builder().user(user).build();
            return cartRepository.save(newcarts);
        });
    }
    public void removeAllOfItem(String username, Integer cartItemId) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Basket is empty"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Basket is empty"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

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
                .orElseThrow(() -> new RuntimeException("User not found"));

        var cart = cartRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new RuntimeException("Basket is empty"));

        var item = cart.getCartItems().stream()
                .filter(ci -> ci.getCartItemId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

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


}
