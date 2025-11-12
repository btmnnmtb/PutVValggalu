package com.example.demo.repository;

import com.example.demo.model.Cosmetic_items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CosmeticItemRepository extends JpaRepository<Cosmetic_items, Integer> {


    @Query(value = """
        select
          count(*)                                       as total_skus,
          coalesce(sum(quantity), 0)                     as total_qty,
          coalesce(sum(price * quantity), 0)             as total_value,

          count(*) filter (where quantity > 10)          as in_stock_skus,
          coalesce(sum(case when quantity > 10 then quantity else 0 end), 0) as in_stock_qty,

          count(*) filter (where quantity between 1 and 10) as low_stock_skus,
          coalesce(sum(case when quantity between 1 and 10 then quantity else 0 end), 0) as low_stock_qty,

          count(*) filter (where quantity = 0)           as out_of_stock_skus
        from cosmetic_items
        """, nativeQuery = true)
    InventoryStats getInventoryStats();
    long countByProductStatusId(Integer productStatusId);

    List<Cosmetic_items> findAllByProductStatusIdOrderByCosmeticItemIdDesc(Integer productStatusId);
}

