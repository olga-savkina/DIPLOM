package org.diplom_back.modules.products.controller;
import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.repository.ClientRepository;
import org.diplom_back.modules.products.entity.*;
import org.diplom_back.modules.products.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor

public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ClientRepository clientRepository;

    @GetMapping("/{productId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdAndIsModeratedTrueOrderByReviewDateDesc(productId));
    }


    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Нужна авторизация");

        // Ищем клиента по email из Principal
        Client client = clientRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        review.setClientId(client.getClientId());
        review.setReviewDate(LocalDateTime.now());
        review.setModerated(false); // Отправляем на модерацию по умолчанию

        return ResponseEntity.ok(reviewRepository.save(review));
    }
}
