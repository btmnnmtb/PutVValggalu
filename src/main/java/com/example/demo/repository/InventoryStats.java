package com.example.demo.repository;

public interface InventoryStats {
    long getTotalSkus();          // кол-во SKU (строк в таблице)
    long getTotalQty();           // суммарное количество единиц товара
    java.math.BigDecimal getTotalValue(); // ∑(price * quantity)

    long getInStockSkus();        // SKU с quantity > 10
    long getLowStockSkus();       // SKU с 1..10
    long getOutOfStockSkus();     // SKU с quantity = 0

    long getInStockQty();         // ∑quantity где quantity > 10
    long getLowStockQty();        // ∑quantity где 1..10
}

