package org.diplom_back.modules.orders.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingAddress;
    private List<CartItemDTO> items; // Список товаров из корзины
    private String paymentMethod; // Добавить
    private String status;        // Добавить для передачи "PAID" или "PENDING"
    private BigDecimal usedBonuses; // Добавить для списания
}