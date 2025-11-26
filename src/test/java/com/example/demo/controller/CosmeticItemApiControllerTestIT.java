package com.example.demo.controller;

import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.Brands;
import com.example.demo.model.Cosmetic_types;
import com.example.demo.model.Manufacturers;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.BrandsRepository;
import com.example.demo.repository.CosmeticTypesRepository;
import com.example.demo.repository.ManufactureRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CosmeticItemApiControllerTestIT {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired CosmeticItemRepository cosmeticItemRepository;
    @Autowired BrandsRepository brandsRepository;
    @Autowired CosmeticTypesRepository cosmeticTypesRepository;
    @Autowired ManufactureRepository manufactureRepository;

    @Autowired ObjectMapper objectMapper;

    private Brands brand;
    private Cosmetic_types cosmeticType;
    private Manufacturers manufacture;

    private Cosmetic_items item1;
    private Cosmetic_items item2;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("""
            TRUNCATE TABLE 
                favourites,
                comments,
                order_items,
                cart_items,
                cosmetic_items
            RESTART IDENTITY CASCADE
            """);

        jdbcTemplate.execute("""
            TRUNCATE TABLE 
                brands,
                cosmetic_types,
                manufacturers
            RESTART IDENTITY CASCADE
            """);

        brand = new Brands();
        brand.setBrandName("TestBrand");
        brand.setBrandPrice(BigDecimal.ZERO);
        brand = brandsRepository.save(brand);

        cosmeticType = new Cosmetic_types();
        cosmeticType.setCosmeticTypeName("Шампунь");
        cosmeticType.setCosmeticTypePrice(BigDecimal.ZERO);
        cosmeticType = cosmeticTypesRepository.save(cosmeticType);

        manufacture = new Manufacturers();
        manufacture.setManufacturerName("TestFactory");
        manufacture.setManufacturerPrice(BigDecimal.ZERO);
        manufacture = manufactureRepository.save(manufacture);

        item1 = new Cosmetic_items();
        item1.setItemName("Шампунь восстанавливающий");
        item1.setDescription("Описание 1");
        item1.setQuantity(10);
        item1.setPrice(BigDecimal.valueOf(500));
        item1.setBrandId(brand.getBrandId());
        item1.setCosmeticTypeId(cosmeticType.getCosmeticTypeId());
        item1.setManufacturerId(manufacture.getManufacturerId());
        item1 = cosmeticItemRepository.save(item1);

        item2 = new Cosmetic_items();
        item2.setItemName("Маска для волос");
        item2.setDescription("Описание 2");
        item2.setQuantity(5);
        item2.setPrice(BigDecimal.valueOf(700));
        item2.setBrandId(brand.getBrandId());
        item2.setCosmeticTypeId(cosmeticType.getCosmeticTypeId());
        item2.setManufacturerId(manufacture.getManufacturerId());
        item2 = cosmeticItemRepository.save(item2);
    }

    @Test
    void getCosmeticItems_returnsAllItems() throws Exception {
        mockMvc.perform(get("/api/cosmetic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cosmeticItemId", is(item1.getCosmeticItemId())))
                .andExpect(jsonPath("$[0].itemName", is(item1.getItemName())));
    }

    @Test
    void getCosmeticItemById_returnsItem() throws Exception {
        mockMvc.perform(get("/api/cosmetic/{id}", item1.getCosmeticItemId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cosmeticItemId", is(item1.getCosmeticItemId())))
                .andExpect(jsonPath("$.itemName", is(item1.getItemName())));
    }

    @Test
    void getCosmeticItemById_notFound_returns5xx() throws Exception {
        mockMvc.perform(get("/api/cosmetic/{id}", 999999))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createCosmetic_createsNewItem() throws Exception {
        String json = """
            {
              "itemName": "Бальзам для волос",
              "description": "Новый товар",
              "quantity": 3,
              "price": 450,
              "brandId": %d,
              "cosmeticTypeId": %d,
              "manufacturerId": %d,
              "productStatusId": null,
              "certificateId": null,
              "imagePath": "/img/balsam.png"
            }
            """.formatted(
                brand.getBrandId(),
                cosmeticType.getCosmeticTypeId(),
                manufacture.getManufacturerId()
        );

        long beforeCount = cosmeticItemRepository.count();

        mockMvc.perform(post("/api/cosmetic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cosmeticItemId").exists())
                .andExpect(jsonPath("$.itemName", is("Бальзам для волос")));

        long afterCount = cosmeticItemRepository.count();
        assertEquals(beforeCount + 1, afterCount);
    }

    @Test
    void updateCosmetic_updatesExistingItem() throws Exception {
        String updatedJson = """
            {
              "itemName": "Шампунь обновлённый",
              "description": "Новое описание",
              "quantity": 20,
              "price": 999,
              "brandId": %d,
              "cosmeticTypeId": %d,
              "manufacturerId": %d,
              "productStatusId": null,
              "certificateId": null,
              "imagePath": "/img/new.png"
            }
            """.formatted(
                brand.getBrandId(),
                cosmeticType.getCosmeticTypeId(),
                manufacture.getManufacturerId()
        );

        mockMvc.perform(put("/api/cosmetic/{id}", item1.getCosmeticItemId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cosmeticItemId", is(item1.getCosmeticItemId())))
                .andExpect(jsonPath("$.itemName", is("Шампунь обновлённый")))
                .andExpect(jsonPath("$.price", is(999)));

        Cosmetic_items reloaded = cosmeticItemRepository.findById(item1.getCosmeticItemId())
                .orElseThrow();
        assertEquals("Шампунь обновлённый", reloaded.getItemName());
        assertEquals(BigDecimal.valueOf(999), reloaded.getPrice());
    }

    @Test
    void deleteCosmetic_deletesExistingItem() throws Exception {
        Integer idToDelete = item2.getCosmeticItemId();

        mockMvc.perform(delete("/api/cosmetic/{id}", idToDelete))
                .andExpect(status().isOk());

        assertFalse(cosmeticItemRepository.existsById(idToDelete));
    }

    @Test
    void deleteCosmetic_notFound_returns5xx() throws Exception {
        mockMvc.perform(delete("/api/cosmetic/{id}", 999999))
                .andExpect(status().is5xxServerError());
    }
}