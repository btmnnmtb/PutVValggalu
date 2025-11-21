package com.example.demo.service;

import com.example.demo.model.CosmeticItemForm;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.ProductStatusRepository;
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
class CosmeticItemsServiceTest {
    @Mock
    ProductStatusRepository productStatusRepository;
    @Mock
    CosmeticItemRepository cosmeticItemRepository;
    @InjectMocks
    CosmeticItemsService cosmeticItemsService;
    @Test
    void updateItemNotFound() {
        when(cosmeticItemRepository.findById(anyInt())).thenReturn(Optional.empty());
        CosmeticItemForm f = new CosmeticItemForm();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cosmeticItemsService.update(1 , f));

        assertEquals("Товар не найден", ex.getMessage());

    }
    @Test
    void deleteItemNotFound() {
        when(cosmeticItemRepository.findById(anyInt())).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> cosmeticItemsService.delete(1));

        assertEquals("Товар не найден" , ex.getMessage());


    }



}