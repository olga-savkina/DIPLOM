package org.diplom_back.modules.products.controller;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.repository.ClientRepository;
import org.diplom_back.modules.products.entity.Review;
import org.diplom_back.modules.products.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewRepository reviewRepository;
    private final ClientRepository clientRepository;

    // --- КЛИЕНТСКАЯ ЧАСТЬ ---

    // Получение только одобренных отзывов для конкретного товара
    @GetMapping("/{productId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdAndIsModeratedTrueOrderByReviewDateDesc(productId));
    }

    // Добавление отзыва (уходит на модерацию)
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Нужна авторизация");

        Client client = clientRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        review.setClientId(client.getClientId());
        review.setReviewDate(LocalDateTime.now());
        review.setModerated(false);

        return ResponseEntity.ok(reviewRepository.save(review));
    }

    // --- АДМИНСКАЯ ЧАСТЬ ---

    // Получить ВСЕ отзывы для админки
    @GetMapping("/admin/all")
    public ResponseEntity<List<Review>> getAllReviewsForAdmin() {
        // Сортируем по дате: самые новые сверху
        return ResponseEntity.ok(reviewRepository.findAllByOrderByReviewDateDesc());
    }

    // Одобрить отзыв (модерация)
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveReview(@PathVariable String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        review.setModerated(true);
        reviewRepository.save(review);
        return ResponseEntity.ok().body("Отзыв одобрен");
    }

    // Удалить отзыв
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        if (!reviewRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reviewRepository.deleteById(id);
        return ResponseEntity.ok().body("Отзыв удален");
    }
}