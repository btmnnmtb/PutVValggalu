package com.example.demo.repository;

import com.example.demo.model.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Integer> {
    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus"})
    List<Orders> findAllByUser_LoginOrderByOrderDateDesc(String login);

    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus", "user"})
    @Query("select o from Orders o order by o.orderDate desc")
    List<Orders> findAllWithItemsOrderByOrderDateDesc();

    @Query("select coalesce(sum(o.total), 0) from Orders o")
    java.math.BigDecimal sumTotalRevenue();

    long countByOrderStatus_OrderStatusIgnoreCase(String status);

    long countByOrderStatus_OrderStatusIgnoreCaseAndUser_Login(String status, String login);

    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus", "user"})
    @Query("""
            select distinct o
            from Orders o
            left join o.items i
            left join i.cosmeticItem ci
            left join o.orderStatus s
            left join o.user u
            where (:q is null
                   or cast(o.orderId as string) like concat('%', :q, '%')
                   or lower(ci.itemName) like lower(concat('%', :q, '%')))
              and (:status is null or lower(s.orderStatus) = lower(:status))
              and (:from is null or o.orderDate >= :from)
            order by o.orderDate desc
            """)
    List<Orders> searchAll(@org.springframework.data.repository.query.Param("q") String q,
                           @org.springframework.data.repository.query.Param("status") String status,
                           @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from);

    @EntityGraph(attributePaths = {"items", "items.cosmeticItem", "orderStatus", "user"})
    @Query("""
            select distinct o
            from Orders o
            left join o.items i
            left join i.cosmeticItem ci
            left join o.orderStatus s
            left join o.user u
            where u.login = :login
              and (:q is null
                   or cast(o.orderId as string) like concat('%', :q, '%')
                   or lower(ci.itemName) like lower(concat('%', :q, '%')))
              and (:status is null or lower(s.orderStatus) = lower(:status))
              and (:from is null or o.orderDate >= :from)
            order by o.orderDate desc
            """)
    List<Orders> searchByUser(@org.springframework.data.repository.query.Param("login") String login,
                              @org.springframework.data.repository.query.Param("q") String q,
                              @org.springframework.data.repository.query.Param("status") String status,
                              @org.springframework.data.repository.query.Param("from") java.time.LocalDateTime from);

    public interface OrderMonthlyAgg {
        String getYm();

        BigDecimal getRevenue();

        Long getCnt();
    }

    @Query(value = """
            select to_char(date_trunc('month', o.order_date),'YYYY-MM') as ym,
                   coalesce(sum(o.total),0) as revenue,
                   count(*) as cnt
            from orders o
            where o.order_date >= coalesce(:fromTs, '-infinity'::timestamp)
            group by 1
            order by 1
            """, nativeQuery = true)
    List<OrderMonthlyAgg> statsRevenueByMonth(@Param("fromTs") Timestamp fromTs);
}




