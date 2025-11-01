package com.example.demo.repository;

import com.example.demo.model.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Integer> {
    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus"})
    List<Orders> findAllByUser_LoginOrderByOrderDateDesc(String login);

    // Все заказы (для админа)
    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus", "user"})
    @Query("select o from Orders o order by o.orderDate desc")
    List<Orders> findAllWithItemsOrderByOrderDateDesc();
    @Query("select coalesce(sum(o.total), 0) from Orders o")
    java.math.BigDecimal sumTotalRevenue();
}

