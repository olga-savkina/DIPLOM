package org.diplom_back.modules.orders.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@Data
public class OrderItem {
    @Id
    @Column(name = "order_item_id")
    private String orderItemId;

    // Оставляем только это объявление поля order
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference // Это предотвратит бесконечную рекурсию в JSON
    private Order order;

    @Column(name = "variant_id")
    private String variantId;

    private Integer quantity;

    @Column(name = "price_at_sale")
    private BigDecimal priceAtSale;
}