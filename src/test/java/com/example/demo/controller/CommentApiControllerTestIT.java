package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.repository.*;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentApiControllerTestIT {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @Autowired CommentsRepository commentsRepository;
    @Autowired UsersRepository usersRepository;
    @Autowired CosmeticItemRepository cosmeticItemRepository;
    @Autowired RolesRepository rolesRepository;

    @Autowired BrandsRepository brandsRepository;
    @Autowired CosmeticTypesRepository cosmeticTypesRepository;
    @Autowired ManufactureRepository manufactureRepository;
    @Autowired PasswordEncoder encoder;

    @Autowired ObjectMapper objectMapper;

    private User testUser;
    private Roles userRole;
    private Cosmetic_items item;
    private Comments comment1;
    private Comments comment2;

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
                manufacturers,
                users,
                roles
            RESTART IDENTITY CASCADE
            """);

        Brands brand = new Brands();
        brand.setBrandName("TestBrand");
        brand.setBrandPrice(BigDecimal.ZERO);
        brand = brandsRepository.save(brand);

        Cosmetic_types cosmeticType = new Cosmetic_types();
        cosmeticType.setCosmeticTypeName("Шампунь");
        cosmeticType = cosmeticTypesRepository.save(cosmeticType);

        Manufacturers manufacture = new Manufacturers();
        manufacture.setManufacturerName("TestFactory");
        manufacture = manufactureRepository.save(manufacture);

        item = new Cosmetic_items();
        item.setItemName("Шампунь восстанавливающий");
        item.setDescription("Описание товара");
        item.setQuantity(10);
        item.setPrice(BigDecimal.valueOf(500));
        item.setBrandId(brand.getBrandId());
        item.setCosmeticTypeId(cosmeticType.getCosmeticTypeId());
        item.setManufacturerId(manufacture.getManufacturerId());
        item = cosmeticItemRepository.save(item);

        userRole = new Roles();
        userRole.setRoleName("Пользователь");
        userRole = rolesRepository.save(userRole);

        testUser = new User();
        testUser.setLogin("Misha");
        testUser.setPasswordHash(encoder.encode("123456"));
        testUser.setRole(userRole);
        testUser = usersRepository.save(testUser);

        comment1 = Comments.builder()
                .user(testUser)
                .cosmeticItem(item)
                .commentText("Отличный шампунь")
                .rating(5)
                .build();
        comment1 = commentsRepository.save(comment1);

        comment2 = Comments.builder()
                .user(testUser)
                .cosmeticItem(item)
                .commentText("Неплохой, но дорогой")
                .rating(4)
                .build();
        comment2 = commentsRepository.save(comment2);
    }

    @Test
    void getComments_returnsAll() throws Exception {
        mockMvc.perform(get("/api/comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].commentText", is(comment1.getCommentText())))
                .andExpect(jsonPath("$[1].commentText", is(comment2.getCommentText())));
    }

    @Test
    void getCommentsById_returnsComment() throws Exception {
        mockMvc.perform(get("/api/comment/{id}", comment1.getCommentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId", is(comment1.getCommentId())))
                .andExpect(jsonPath("$.commentText", is(comment1.getCommentText())))
                .andExpect(jsonPath("$.rating", is(comment1.getRating())));
    }

    @Test
    void getCommentsById_notFound_returns5xx() throws Exception {
        // Контроллер бросает RuntimeException, глобального хендлера нет -> 500
        mockMvc.perform(get("/api/comment/{id}", 999999))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void createComment_createsNewComment() throws Exception {

        long beforeCount = commentsRepository.count();

        String json = """
            {
              "userId": %d,
              "cosmeticItemId": %d,
              "commentText": "   Новый комментарий  ",
              "rating": 3
            }
            """.formatted(testUser.getUserId(), item.getCosmeticItemId());

        mockMvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").exists())
                .andExpect(jsonPath("$.commentText", is("Новый комментарий")))
                .andExpect(jsonPath("$.rating", is(3)));

        long afterCount = commentsRepository.count();
        assertEquals(beforeCount + 1, afterCount);
    }

    @Test
    void createComment_userNotFound_returnsBadRequest() throws Exception {
        String json = """
            {
              "userId": 999999,
              "cosmeticItemId": %d,
              "commentText": "Комментарий",
              "rating": 5
            }
            """.formatted(item.getCosmeticItemId());

        mockMvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Пользователь не найден: id=999999")));
    }

    @Test
    void createComment_itemNotFound_returnsBadRequest() throws Exception {
        String json = """
            {
              "userId": %d,
              "cosmeticItemId": 999999,
              "commentText": "Комментарий",
              "rating": 5
            }
            """.formatted(testUser.getUserId());

        mockMvc.perform(post("/api/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Товар не найден: id=999999")));
    }

    @Test
    void deleteComment_deletesExisting() throws Exception {
        Integer idToDelete = comment2.getCommentId();

        mockMvc.perform(delete("/api/comment/{id}", idToDelete))
                .andExpect(status().isOk())
                .andExpect(content().string("Комемнтарий удален "));

        assertFalse(commentsRepository.existsById(idToDelete));
    }

    @Test
    void deleteComment_notFound_stillReturnsOk() throws Exception {
        long before = commentsRepository.count();

        mockMvc.perform(delete("/api/comment/{id}", 999999))
                .andExpect(status().isOk())
                .andExpect(content().string("Комемнтарий удален "));

        long after = commentsRepository.count();
        assertEquals(before, after);
    }
}
