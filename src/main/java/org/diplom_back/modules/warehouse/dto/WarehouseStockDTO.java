package org.diplom_back.modules.warehouse.dto;

import java.time.LocalDate;

// WarehouseStockDTO.java
public class WarehouseStockDTO {
    private String variantId;
    private String productName;
    private String sku;
    private int quantity;
    private int reservedQuantity;
    private LocalDate bestBefore; // Берем из ProductVariant
    // геттеры и сеттеры
}
