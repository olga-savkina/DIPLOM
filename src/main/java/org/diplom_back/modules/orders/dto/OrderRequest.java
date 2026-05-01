package org.diplom_back.modules.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String shippingAddress;
    private List<CartItemDTO> items; // Список товаров из корзины
}