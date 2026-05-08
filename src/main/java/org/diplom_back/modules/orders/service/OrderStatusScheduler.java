package org.diplom_back.modules.orders.service;

import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderStatus;
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStatusScheduler {

    @Autowired
    private OrderRepository orderRepository;

    // Запускать каждые 3 часа (время в миллисекундах)
    @Scheduled(cron = "0 5 3 * * *")
    public void autoUpdateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Если оплачен и прошло больше 24 часов -> "Отправлен"
        List<Order> paidOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.PAID, now.minusDays(1)
        );
        for (Order order : paidOrders) {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }

        // 2. Если в статусе "Отправлен" больше 3 дней -> "Доставлен" (или "Завершен")
        List<Order> shippedOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.SHIPPED, now.minusDays(3)
        );
        for (Order order : shippedOrders) {
            order.setStatus(OrderStatus.DELIVERED);
            orderRepository.save(order);
        }

        System.out.println("Автоматическое обновление статусов завершено в " + now);
    }
}
