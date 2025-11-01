package com.example.demo.repository;

import com.example.demo.model.Carts;
import com.example.demo.model.Favourites;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavourRepository extends JpaRepository<Favourites, Integer> {
    boolean  existsByUser_UserIdAndCosmeticItem_CosmeticItemId(Integer userId,  Integer cosmeticItemId);
    Optional<Favourites> findByUser_UserIdAndCosmeticItem_CosmeticItemId(Integer userId, Integer cosmeticItemId);
    void deleteByUser_UserIdAndCosmeticItem_CosmeticItemId(Integer userId, Integer cosmeticItemId);
    List<Favourites> findAllByUser_UserId(Integer userId);
}
