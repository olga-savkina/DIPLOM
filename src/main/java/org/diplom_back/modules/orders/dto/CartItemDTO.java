package org.diplom_back.modules.orders.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private String variantId;
    private Integer quantity;
    private BigDecimal price; // Цена на момент продажи (priceAtSale)
}