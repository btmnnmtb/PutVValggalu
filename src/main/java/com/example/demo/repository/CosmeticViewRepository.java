package com.example.demo.repository;

import com.example.demo.model.CosmeticView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CosmeticViewRepository extends JpaRepository<CosmeticView, Integer> {
    List<CosmeticView> findByBrandName(String brandName);
    List<CosmeticView> findByManufacturerName(String manufacturerName);
    List<CosmeticView> findByStatusName(String statusName);
    long countByStatusName(String statusName);
    List<CosmeticView> findAllByStatusNameOrderByCosmeticItemIdDesc(String statusName);

    @Query("""
        select v.cosmeticTypeName as name, count(v) as cnt
        from CosmeticView v
        group by v.cosmeticTypeName
        order by count(v) desc
    """)
    List<CosmeticView.NameCount> countSkuByType();

    // делаем нативный SQL, чтобы честно ограничить TOP-10
    @Query(value = """
        select brand_name as name, sum(price * quantity) as sum
        from cosmetic_table
        group by brand_name
        order by sum(price * quantity) desc
        limit 10
    """, nativeQuery = true)
    List<CosmeticView.NameSum> topBrandsByStockValue();

    @Query("""
        select sum(case when v.quantity = 0 then 1 else 0 end) as zeroQty,
               sum(case when v.quantity between 1 and 5 then 1 else 0 end) as low1to5,
               sum(case when v.quantity between 6 and 10 then 1 else 0 end) as low6to10,
               sum(case when v.quantity > 10 then 1 else 0 end) as gt10
        from CosmeticView v
    """)
    Object distributionBuckets();
}


