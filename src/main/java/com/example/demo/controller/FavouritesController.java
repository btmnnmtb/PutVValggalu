package com.example.demo.controller;

import com.example.demo.repository.BrandsRepository;
import com.example.demo.repository.CosmeticTypesRepository;
import com.example.demo.repository.FavourRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.FavourService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequiredArgsConstructor
@RequestMapping("/favourites")
public class FavouritesController {
    private final FavourService favourService;
    private final UsersRepository usersRepository;
    private final BrandsRepository brandsRepository;
    private final CosmeticTypesRepository cosmeticTypesRepository;

    @GetMapping
    public String page(Authentication auth, Model model) {
        String username = auth.getName();
        var user = usersRepository.findByLogin(username).orElse(null);
        String role = user != null ? user.getRole().getRoleName().trim() : "неизвестно";

        model.addAttribute("username", username);
        model.addAttribute("roles", role);
        model.addAttribute("brands", brandsRepository.findAll());
        model.addAttribute("types", cosmeticTypesRepository.findAll());
        model.addAttribute("favorites",
                favourService.getUserFavourites(username));

        return "favorites";
    }

    @PostMapping("/{itemId}/toggle")
    public String toggle(@PathVariable Integer itemId,
                         Authentication auth,
                         @RequestParam(value = "redirect", defaultValue = "/Catalog") String redirect) {
        favourService.toggleFavourite(itemId, auth.getName());
        return "redirect:" + redirect;
    }

    @PostMapping("/{itemId}/add")
    public String add(@PathVariable Integer itemId,
                      Authentication auth,
                      @RequestParam(value = "redirect", defaultValue = "/Catalog") String redirect) {
        favourService.addToFavourites(itemId, auth.getName());
        return "redirect:" + redirect;
    }

    @PostMapping("/{itemId}/remove")
    public String remove(@PathVariable Integer itemId,
                         Authentication auth,
                         @RequestParam(value = "redirect", defaultValue = "/favourites") String redirect) {
        favourService.removeFromFavourites(itemId, auth.getName());
        return "redirect:" + redirect;
    }
}
