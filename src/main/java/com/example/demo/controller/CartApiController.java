package com.example.demo.controller;

import com.example.demo.model.AddItemRequest;
import com.example.demo.model.Carts;
import com.example.demo.model.CartsDto;
import com.example.demo.repository.CartRepository;
import com.example.demo.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartApiController {
    private final CartRepository cartRepository;
    private final CartService cartService;
    @GetMapping()
    public List<CartsDto> getAllCarts() {
        return cartRepository.findAll()
                .stream()
                .map(CartsDto::fromEntity)
                .toList();
        }
    @GetMapping("/{id}")
    public Carts getCart(@PathVariable Integer id){
        return cartRepository.findByUser_UserId(id).get();
    }
    @PostMapping("/{username}/items")
    public CartsDto addItem(@PathVariable String username,
                            @RequestBody AddItemRequest req) {
        var qty = (req.getQuantity() == null || req.getQuantity() < 1) ? 1 : req.getQuantity();
        cartService.addCart(req.getCosmeticItemId(), username, qty);
        return CartsDto.fromEntity(cartService.getCart(username));
    }

    @PatchMapping("/{username}/items/{cartItemId}/inc")
    public CartsDto increment(@PathVariable String username,
                              @PathVariable Integer cartItemId) {
        cartService.incrementOne(username, cartItemId);
        return CartsDto.fromEntity(cartService.getCart(username));
    }

    @PatchMapping("/{username}/items/{cartItemId}/dec")
    public CartsDto decrement(@PathVariable String username,
                              @PathVariable Integer cartItemId) {
        cartService.decrementOne(username, cartItemId);
        return CartsDto.fromEntity(cartService.getCart(username));
    }

    @DeleteMapping("/{username}/items/{cartItemId}")
    public CartsDto removeItem(@PathVariable String username,
                               @PathVariable Integer cartItemId) {
        cartService.removeAllOfItem(username, cartItemId);
        return CartsDto.fromEntity(cartService.getCart(username));
    }

    @DeleteMapping("/{username}/items")
    public CartsDto clear(@PathVariable String username) {
        cartService.clearCart(username);
        return CartsDto.fromEntity(cartService.getCart(username));
    }
}


