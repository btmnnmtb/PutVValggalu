package com.example.demo.controller;

import com.example.demo.model.Comments;
import com.example.demo.model.User;
import com.example.demo.repository.*;
import com.example.demo.service.CartService;
import com.example.demo.service.CommentsService;
import com.example.demo.service.FavourService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final CosmeticViewRepository cosmeticViewRepository;
    private final UsersRepository usersRepository;
    private final BrandsRepository brandsRepository;
    private final CosmeticTypesRepository cosmeticTypesRepository;
    private final CartService cartService;
    private final FavourService favourService;
    private final CommentsService commentsService;

    @GetMapping("/Catalog")
    public String testPage(Model model, Authentication authentication) {
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();

            model.addAttribute("items", cosmeticViewRepository.findAll());
            model.addAttribute("brands", brandsRepository.findAll());
            model.addAttribute("types", cosmeticTypesRepository.findAll());
            Set<Integer> favIds = favourService.getFavouriteIds(username);
            model.addAttribute("favouriteIds", favIds);

            User user = usersRepository.findByLogin(username).orElse(null);
            String role = (user != null) ? user.getRole().getRoleName().trim() : "неизвестно";
            model.addAttribute("username", username);
            model.addAttribute("role", role);

            Map<Integer, List<Comments>> commentsByItem = commentsService.findAllGroupedByCosmeticItemId();
            model.addAttribute("commentsByItem", commentsByItem);
        }
        else{
            model.addAttribute("username", "Гость");
            model.addAttribute("roles", "гость");
            model.addAttribute("items", cosmeticViewRepository.findAll());
            model.addAttribute("brands", brandsRepository.findAll());
            model.addAttribute("types", cosmeticTypesRepository.findAll());
            Map<Integer, List<Comments>> commentsByItem = commentsService.findAllGroupedByCosmeticItemId();
            model.addAttribute("commentsByItem", commentsByItem);

        }


        return "Catalog";
    }

    @PostMapping("/cart/add/{cosmeticItemId}")
    public String addToCart(@PathVariable("cosmeticItemId") Integer cosmeticItemId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            Authentication authentication) {
        String username = authentication.getName();
        cartService.addCart(cosmeticItemId, username, quantity);
        return "redirect:/Catalog";
    }
}