package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavourService {
    private final FavourRepository favourRepository;
    private final UsersRepository usersRepository;
    private final CosmeticItemRepository cosmeticItemRepository;

    @Transactional(readOnly = true)
    public Set<Integer> getFavouriteIds(String username) {
        var user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return favourRepository.findAllByUser_UserId(user.getUserId())
                .stream()
                .map(f -> f.getCosmeticItem().getCosmeticItemId())
                .collect(Collectors.toSet());
    }
    @Transactional
    public boolean addToFavourites(Integer cosmeticItemId, String username) {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cosmetic_items item = cosmeticItemRepository.findById(cosmeticItemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        boolean exists = favourRepository
                .existsByUser_UserIdAndCosmeticItem_CosmeticItemId(user.getUserId(), cosmeticItemId);
        if (exists) return false;

        Favourites fav = Favourites.builder()
                .user(user)
                .cosmeticItem(item)
                .build();
        favourRepository.save(fav);
        return true;
    }
    public boolean removeFromFavourites(Integer cosmeticItemId, String username) {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        var existing = favourRepository
                .findByUser_UserIdAndCosmeticItem_CosmeticItemId(user.getUserId(), cosmeticItemId);
        if (existing.isEmpty()) return false;

        favourRepository.delete(existing.get());
        return true;
    }
    @Transactional
    public boolean toggleFavourite(Integer cosmeticItemId, String username) {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = favourRepository
                .existsByUser_UserIdAndCosmeticItem_CosmeticItemId(user.getUserId(), cosmeticItemId);
        if (exists) {
            favourRepository.deleteByUser_UserIdAndCosmeticItem_CosmeticItemId(user.getUserId(), cosmeticItemId);
            return false;
        } else {
            Cosmetic_items item = cosmeticItemRepository.findById(cosmeticItemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            favourRepository.save(Favourites.builder().user(user).cosmeticItem(item).build());
            return true;
        }

    }

    public List<Cosmetic_items> getUserFavourites(String username) {
        User user = usersRepository.findByLogin(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return favourRepository.findAllByUser_UserId(user.getUserId())
                .stream()
                .map(Favourites::getCosmeticItem)
                .toList();
    }


}
