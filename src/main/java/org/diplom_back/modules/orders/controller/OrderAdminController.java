package org.diplom_back.modules.orders.controller;

import lombok.*;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.OrderRequest;
import org.diplom_back.modules.orders.dto.OrderResponseDTO;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.diplom_back.modules.orders.service.*;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService; // Внедряем сервис, где лежит логика конвертации

    @GetMapping
    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));

        // Вызываем метод конвертации из orderService
        return orders.stream()
                .map(orderService::convertToResponseDTO)
                .toList();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String id, @RequestBody Map<String, String> payload) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        String newStatus = payload.get("status");
        order.setStatus(newStatus);
        orderRepository.save(order);

        return ResponseEntity.ok("Статус обновлен");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable String id) {
        if (!orderRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Заказ не найден");
        }
        orderRepository.deleteById(id);
        return ResponseEntity.ok("Заказ удален");
    }
}