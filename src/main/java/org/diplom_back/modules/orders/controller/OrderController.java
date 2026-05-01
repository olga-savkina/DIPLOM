package org.diplom_back.modules.orders.controller;

import lombok.*;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.OrderRequest;
import org.diplom_back.modules.orders.dto.OrderResponseDTO;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.service.*;
import org.springframework.http.*;
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
}