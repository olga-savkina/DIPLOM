package org.diplom_back.modules.orders.controller;

import jakarta.transaction.Transactional;
import lombok.*;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.OrderRequest;
import org.diplom_back.modules.orders.dto.OrderResponseDTO;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.diplom_back.modules.orders.service.*;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;

    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order newOrder = orderService.createOrder(request, user);
        return ResponseEntity.ok(newOrder);
    }
    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Сервис теперь возвращает уже готовые данные с названиями
        return ResponseEntity.ok(orderService.getUserOrders(principal.getName()));
    }
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId, Principal principal) {
        try {
            orderService.cancelOrder(orderId, principal.getName());
            return ResponseEntity.ok("Заказ успешно отменен");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}