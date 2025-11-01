package com.example.demo.controller;

import com.example.demo.model.Carts;
import com.example.demo.model.User;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final UsersRepository usersRepository;
    private final CartService cartService;
    private final OrderService orderService;
    @GetMapping("/Cart")
    public String cartPage(Authentication authentication , Model model) {
        String username = authentication.getName();
        User user = usersRepository.findByLogin(username).orElse(null);
        String role = (user !=null) ? user.getRole().getRoleName().trim() : "неизвестно";
        Carts carts = cartService.getCart(username);
        model.addAttribute("username", username);
        model.addAttribute("role", role);
        model.addAttribute("cart", carts);
        return "Cart";
    }
    @PostMapping("/Delete/{itemId}")
    public String deleteAll(@PathVariable("itemId") Integer itemId, Authentication auth) {
        cartService.removeAllOfItem(auth.getName(), itemId);
        return "redirect:/Cart";
    }

    @PostMapping("/cart/items/{itemId}/decrement")
    public String decrement(@PathVariable("itemId") Integer itemId, Authentication auth) {
        cartService.decrementOne(auth.getName(), itemId);
        return "redirect:/Cart";
    }

    @PostMapping("/cart/items/{itemId}/increment")
    public String increment(@PathVariable("itemId") Integer itemId, Authentication auth) {
        cartService.incrementOne(auth.getName(), itemId);
        return "redirect:/Cart";
    }
    @PostMapping("/orders/checkout")
    public String checkout(Authentication auth, Model model) {
        Integer orderId = orderService.checkout(auth.getName());
        return "redirect:/orders";
    }
}
