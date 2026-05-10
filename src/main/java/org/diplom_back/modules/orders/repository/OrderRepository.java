package org.diplom_back.modules.orders.repository;

import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByClientUserEmail(String email);
    // Найти заказы за определенный период (для графиков)
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    List<Order> findByStatusAndUpdatedAtBefore(OrderStatus status, LocalDateTime dateTime);
    // Считать общую выручку прямо в базе
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status != 'CANCELLED'")
    Double getTotalRevenue();
    long countByClient_User(User user);
}

