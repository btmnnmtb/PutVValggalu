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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderApiControllerTestIt {

    @Autowired MockMvc mockMvc;
    @Autowired UsersRepository usersRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderStatusRepository orderStatusRepository;
    @Autowired CosmeticItemRepository cosmeticItemRepository;
    @Autowired RolesRepository rolesRepository;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private Roles userRole;
    private Order_status pendingStatus;
    private Cosmetic_items product;

    private Orders existingOrder;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM order_items");
        jdbcTemplate.execute("DELETE FROM orders");
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

        pendingStatus = new Order_status();
        pendingStatus.setOrderStatus("Pending");
        pendingStatus = orderStatusRepository.save(pendingStatus);

        product = new Cosmetic_items();
        product.setItemName("Шампунь");
        product.setPrice(BigDecimal.valueOf(500));
        product.setQuantity(10);
        product = cosmeticItemRepository.save(product);

        existingOrder = new Orders();
        existingOrder.setUser(testUser);
        existingOrder.setOrderStatus(pendingStatus);
        existingOrder.setOrderDate(new java.util.Date());
        existingOrder = orderRepository.save(existingOrder);
    }

    @Test
    void getAllOrders_returnsOkAndList() throws Exception {
        mockMvc.perform(get("/api/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(testUser.getUserId()));
    }

    @Test
    void getOrdersByUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/order/{login}/user", "Misha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(testUser.getUserId()));
    }

    @Test
    void getOrder_validId_returnsOk() throws Exception {
        mockMvc.perform(get("/api/order/{id}", existingOrder.getOrderId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()));
    }

    @Test
    void createOrder_validData_returnsSavedOrder() throws Exception {
        OrderDto dto = new OrderDto();
        dto.setUserId(testUser.getUserId());
        dto.setStatusId(pendingStatus.getOrdersStatusId());

        OrderDto.OrderItemDto item = new OrderDto.OrderItemDto();
        item.setCosmeticItemId(product.getCosmeticItemId());
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(500));

        dto.setItems(List.of(item));

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value(testUser.getUserId()));
    }

    @Test
    void updateOrderStatus_validData_returnsUpdatedOrder() throws Exception {
        OrderDto dto = new OrderDto();
        dto.setStatusId(pendingStatus.getOrdersStatusId());

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/api/order/{id}/status", existingOrder.getOrderId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusId").value(pendingStatus.getOrdersStatusId()));
    }
}
