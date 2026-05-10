package org.diplom_back.modules.products.controller;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.products.DTO.ReviewDTO;
import org.diplom_back.modules.products.entity.Review;
import org.diplom_back.modules.products.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // Чтобы фронт не ругался на CORS
public class ReviewController {

    private final ReviewService reviewService;

    // --- ПУБЛИЧНЫЕ И КЛИЕНТСКИЕ ---

    @GetMapping("/{productId}")
    public ResponseEntity<List<Review>> getReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getApprovedReviewsByProduct(productId));
    }

    @GetMapping("/public/approved")
    public List<ReviewDTO> getApprovedReviews() {
        return reviewService.getOnlyApproved();
    }

    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody Review review, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Нужна авторизация");
        return ResponseEntity.ok(reviewService.addReview(review, principal.getName()));
    }

    // --- АДМИНСКИЕ ---

    @GetMapping("/admin/all")
    public ResponseEntity<List<Review>> getAllReviewsForAdmin() {
        return ResponseEntity.ok(reviewService.getAllForAdmin());
    }

    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<?> approveReview(@PathVariable String id) {
        reviewService.approveReview(id);
        return ResponseEntity.ok().body("Отзыв одобрен");
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok().body("Отзыв удален");
    }
}