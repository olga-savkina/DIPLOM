package org.diplom_back.modules.orders.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.diplom_back.modules.auth.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "`order`") // order - зарезервированное слово в SQL, лучше в кавычках
@Data
public class Order {
    @Id
    @Column(name = "order_id")
    private String orderId;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING) // Сохраняет в БД "PAID", а не порядковый номер 0, 1, 2
    @Column(name = "status")
    private OrderStatus status; // Замени String на OrderStatus

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;
    @Column(name = "payment_method")
    private String paymentMethod; // Добавляем это поле

    @Column(name = "used_bonuses")
    private Integer usedBonuses;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonManagedReference // "Главная" сторона, которая будет отображаться в JSON
    private List<OrderItem> items;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Чтобы дата обновлялась автоматически перед сохранением
    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}