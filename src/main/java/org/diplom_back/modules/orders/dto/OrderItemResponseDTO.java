package org.diplom_back.modules.orders.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponseDTO {
    private String variantId;
    private Integer quantity;
    private String color; // Добавили
    private String size;  // Добавили
    private String sku;
    private BigDecimal priceAtSale;
    private String productName; // <-- Вот то, что нам нужно на фронтенде
}
