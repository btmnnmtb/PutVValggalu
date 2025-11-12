package com.example.demo.controller;


import com.example.demo.model.FavouriteDto;
import com.example.demo.repository.FavourRepository;
import com.example.demo.service.FavourService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouritesApiController {

    private final FavourService favourService;
    private final FavourRepository favourRepository;


    @GetMapping("/{username}")
    public List<FavouriteDto> getUserFavourites(@PathVariable String username) {
        var userFavs = favourService.getUserFavourites(username); // список Cosmetic_items
        return favourRepository.findAll().stream()
                .filter(f -> f.getUser().getLogin().equals(username))
                .map(FavouriteDto::fromEntity)
                .toList();
    }

    @GetMapping("/{username}/ids")
    public Set<Integer> getFavouriteIds(@PathVariable String username) {
        return favourService.getFavouriteIds(username);
    }

    @PostMapping("/{username}/add/{cosmeticItemId}")
    public boolean add(@PathVariable String username, @PathVariable Integer cosmeticItemId) {
        return favourService.addToFavourites(cosmeticItemId, username);
    }

    @DeleteMapping("/{username}/remove/{cosmeticItemId}")
    public boolean remove(@PathVariable String username, @PathVariable Integer cosmeticItemId) {
        return favourService.removeFromFavourites(cosmeticItemId, username);
    }

    @PostMapping("/{username}/toggle/{cosmeticItemId}")
    public boolean toggle(@PathVariable String username, @PathVariable Integer cosmeticItemId) {
        return favourService.toggleFavourite(cosmeticItemId, username);
    }
}