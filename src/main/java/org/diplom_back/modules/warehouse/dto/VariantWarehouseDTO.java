package org.diplom_back.modules.warehouse.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VariantWarehouseDTO {
    private String variantId;
    private String sku;
    private String size;
    private String color;
    private BigDecimal priceOverride;
    private Integer ageMin;
    private Integer ageMax;
    // Данные из таблицы WarehouseStock (вместо старого stockQuantity)
    private Integer quantity;
    private Integer reservedQuantity;
    private LocalDateTime lastUpdated;
}