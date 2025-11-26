package com.example.demo.controller;

import com.example.demo.model.Cosmetic_items;
import com.example.demo.model.Roles;
import com.example.demo.model.User;
import com.example.demo.repository.CosmeticItemRepository;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UsersRepository;
import com.example.demo.service.FavourService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FavouritesApiControllerTestit {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;
    @Autowired RolesRepository rolesRepository;
    @Autowired CosmeticItemRepository cosmeticItemRepository;
    @Autowired FavourService favourService;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbcTemplate;

    private User testUser;
    private Roles userRole;
    private Cosmetic_items item1;
    private Cosmetic_items item2;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM favourites");
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM cosmetic_items");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM roles");


        userRole = new Roles();
        userRole.setRoleName("Пользователь");
        userRole = rolesRepository.save(userRole);

        testUser = new User();
        testUser.setLogin("Misha");
        testUser.setPasswordHash(encoder.encode("123456"));
        testUser.setRole(userRole);
        testUser = usersRepository.save(testUser);

        item1 = new Cosmetic_items();
        item1.setItemName("Шампунь");
        item1.setPrice(BigDecimal.valueOf(500));
        item1.setQuantity(10);
        item1 = cosmeticItemRepository.save(item1);

        item2 = new Cosmetic_items();
        item2.setItemName("Маска для волос");
        item2.setPrice(BigDecimal.valueOf(700));
        item2.setQuantity(5);
        item2 = cosmeticItemRepository.save(item2);

        favourService.addToFavourites(item1.getCosmeticItemId(), testUser.getLogin());
    }

    @Test
    void getUserFavourites_returnsOkAndListForUser() throws Exception {
        mockMvc.perform(get("/api/favourites/{username}", testUser.getLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cosmeticItemId", is(item1.getCosmeticItemId())));
    }

    @Test
    void getFavouriteIds_returnsIdsOfUserFavourites() throws Exception {
        mockMvc.perform(get("/api/favourites/{username}/ids", testUser.getLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]", is(item1.getCosmeticItemId())));
    }

    @Test
    void add_addsItemToFavouritesAndReturnsTrue() throws Exception {
        mockMvc.perform(post("/api/favourites/{username}/add/{cosmeticItemId}",
                        testUser.getLogin(), item2.getCosmeticItemId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Проверяем через сервис, что теперь в избранном есть второй товар
        var ids = favourService.getFavouriteIds(testUser.getLogin());
        assertTrue(ids.contains(item2.getCosmeticItemId()));
    }

    @Test
    void remove_removesItemFromFavouritesAndReturnsTrue() throws Exception {
        // Убедимся, что item1 уже в избранном (мы добавляем его в @BeforeEach)
        assertTrue(favourService.getFavouriteIds(testUser.getLogin())
                .contains(item1.getCosmeticItemId()));

        mockMvc.perform(delete("/api/favourites/{username}/remove/{cosmeticItemId}",
                        testUser.getLogin(), item1.getCosmeticItemId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        var ids = favourService.getFavouriteIds(testUser.getLogin());
        assertFalse(ids.contains(item1.getCosmeticItemId()));
    }

    @Test
    void toggle_togglesFavouriteState() throws Exception {
        // item2 изначально не в избранном
        assertFalse(favourService.getFavouriteIds(testUser.getLogin())
                .contains(item2.getCosmeticItemId()));

        // Первый toggle — добавляет
        mockMvc.perform(post("/api/favourites/{username}/toggle/{cosmeticItemId}",
                        testUser.getLogin(), item2.getCosmeticItemId()))
                .andExpect(status().isOk());

        assertTrue(favourService.getFavouriteIds(testUser.getLogin())
                .contains(item2.getCosmeticItemId()));

        // Второй toggle — убирает
        mockMvc.perform(post("/api/favourites/{username}/toggle/{cosmeticItemId}",
                        testUser.getLogin(), item2.getCosmeticItemId()))
                .andExpect(status().isOk());

        assertFalse(favourService.getFavouriteIds(testUser.getLogin())
                .contains(item2.getCosmeticItemId()));
    }
}
