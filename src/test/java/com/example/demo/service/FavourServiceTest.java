package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.FavourRepository;
import com.example.demo.repository.UsersRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)

class FavourServiceTest {
    @Mock
    UsersRepository usersRepository;
    @Mock
    FavourRepository favourRepository;
    @Mock
    CosmeticItemRepository cosmeticItemRepository;
    @InjectMocks
    FavourService favourService;

    @Test
    void getFavouriteIdsUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> favourService.getFavouriteIds(username));
        Assertions.assertEquals("User not found", ex.getMessage());


    }

    @Test
    void addToFavouritesUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> favourService.addToFavourites(anyInt(),username));
        Assertions.assertEquals("User not found", ex.getMessage());
    }
    @Test
    void addToFavouritesItemNotFound() {
        String username = "username";
        User user = new User();
        user.setUserId(1);
        when(usersRepository.findByLogin(username)).thenReturn(Optional.of(user));

        when(cosmeticItemRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, ()-> favourService.addToFavourites(1,username));

        Assertions.assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void removeFromFavouritesUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());

        RuntimeException ex =  assertThrows(RuntimeException.class, () -> favourService.removeFromFavourites(1,username));
        Assertions.assertEquals("User not found", ex.getMessage());
    }

    @Test
    void toggleFavouriteUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> favourService.toggleFavourite(anyInt(),username));
        Assertions.assertEquals("User not found", ex.getMessage());
    }
    @Test
    void toggleFavouritesItemNotFound() {
        String username = "username";
        User user = new User();
        user.setUserId(1);
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.of(user));

        when(cosmeticItemRepository.findById(anyInt())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> favourService.toggleFavourite(anyInt(),username));
        Assertions.assertEquals("Item not found", ex.getMessage());
    }

    @Test
    void getUserFavouritesUserNotFound() {
        String username = "username";
        when(usersRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> favourService.getUserFavourites(username));
        Assertions.assertEquals("User not found", ex.getMessage());
    }
}