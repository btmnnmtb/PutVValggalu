package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartApiControllerTestIT {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired UsersRepository usersRepository;
    @Autowired RolesRepository rolesRepository;
    @Autowired CosmeticItemRepository cosmeticItemRepository;
    @Autowired BrandsRepository brandsRepository;
    @Autowired CosmeticTypesRepository cosmeticTypesRepository;
    @Autowired ManufactureRepository manufactureRepository;
    @Autowired CartRepository cartRepository;

    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired CartService cartService;

    private User user;
    private Roles role;
    private Cosmetic_items item;

    @BeforeEach
    void setUp() {

        jdbcTemplate.execute("""
            TRUNCATE TABLE 
                cart_items,
                carts,
                comments,
                cosmetic_items,
                brands,
                cosmetic_types,
                manufacturers,
                users,
                roles
            RESTART IDENTITY CASCADE
        """);

        role = new Roles();
        role.setRoleName("USER");
        role = rolesRepository.save(role);

        user = new User();
        user.setLogin("misha");
        user.setPasswordHash(encoder.encode("123456"));
        user.setRole(role);
        user = usersRepository.save(user);

        Brands brand = new Brands();
        brand.setBrandName("Brand");
        brand.setBrandPrice(BigDecimal.ZERO);
        brand = brandsRepository.save(brand);

        Cosmetic_types type = new Cosmetic_types();
        type.setCosmeticTypeName("Шампунь");
        type = cosmeticTypesRepository.save(type);

        Manufacturers manufacture = new Manufacturers();
        manufacture.setManufacturerName("Zavod");
        manufacture = manufactureRepository.save(manufacture);

        item = new Cosmetic_items();
        item.setItemName("Шампунь");
        item.setDescription("Описание");
        item.setPrice(BigDecimal.valueOf(500));
        item.setQuantity(10);
        item.setBrandId(brand.getBrandId());
        item.setCosmeticTypeId(type.getCosmeticTypeId());
        item.setManufacturerId(manufacture.getManufacturerId());
        item = cosmeticItemRepository.save(item);

        cartService.getCart("misha");
    }


    @Test
    void getAllCarts_returnsList() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))  // одна корзина — пользователя misha
                .andExpect(jsonPath("$[0].username", is("misha")));
    }


    @Test
    void getCart_returnsUserCart() throws Exception {
        mockMvc.perform(get("/api/cart/{id}", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId", is(user.getUserId())));
    }


    @Test
    void addItem_addsToCart() throws Exception {

        String json = """
            {
              "cosmeticItemId": %d,
              "quantity": 2
            }
            """.formatted(item.getCosmeticItemId());

        mockMvc.perform(post("/api/cart/{username}/items", "misha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }


    @Test
    void increment_increasesQuantity() throws Exception {

        cartService.addCart(item.getCosmeticItemId(), "misha", 1);
        Integer cartItemId = cartRepository.findByUser_UserId(user.getUserId())
                .get().getCartItems().get(0).getCartItemId();

        mockMvc.perform(patch("/api/cart/{username}/items/{id}/inc", "misha", cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }


    @Test
    void decrement_decreasesQuantity() throws Exception {

        cartService.addCart(item.getCosmeticItemId(), "misha", 3);
        Integer cartItemId = cartRepository.findByUser_UserId(user.getUserId())
                .get().getCartItems().get(0).getCartItemId();

        mockMvc.perform(patch("/api/cart/{username}/items/{id}/dec", "misha", cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }


    @Test
    void removeItem_removesCompletely() throws Exception {
        cartService.addCart(item.getCosmeticItemId(), "misha", 1);
        Integer cartItemId = cartRepository.findByUser_UserId(user.getUserId())
                .get().getCartItems().get(0).getCartItemId();

        mockMvc.perform(delete("/api/cart/{username}/items/{id}", "misha", cartItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }


    @Test
    void clear_removesAllItems() throws Exception {
        cartService.addCart(item.getCosmeticItemId(), "misha", 1);
        cartService.addCart(item.getCosmeticItemId(), "misha", 1);

        mockMvc.perform(delete("/api/cart/{username}/items", "misha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
