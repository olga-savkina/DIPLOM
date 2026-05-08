package org.diplom_back.modules.orders.entity;

public enum OrderStatus {
    PENDING,    // Ожидает оплаты
    PAID,       // Оплачен
    SHIPPED,    // Отправлен
    DELIVERED,  // Доставлен
    CANCELLED   // Отменен
}