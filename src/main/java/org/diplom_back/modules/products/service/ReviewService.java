package org.diplom_back.modules.products.service;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.repository.ClientRepository;
import org.diplom_back.modules.products.DTO.ReviewDTO;
import org.diplom_back.modules.products.entity.Review;
import org.diplom_back.modules.products.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Автоматически внедряет финальные поля (репозитории)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ClientRepository clientRepository;

    // --- КЛИЕНТСКИЕ МЕТОДЫ ---

    public List<Review> getApprovedReviewsByProduct(String productId) {
        return reviewRepository.findByProductIdAndIsModeratedTrueOrderByReviewDateDesc(productId);
    }

    public List<ReviewDTO> getOnlyApproved() {
        return reviewRepository.findByisModeratedTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Review addReview(Review review, String email) {
        Client client = clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        review.setClient(client);
        review.setReviewDate(LocalDateTime.now());
        review.setModerated(false);
        return reviewRepository.save(review);
    }

    // --- АДМИНСКИЕ МЕТОДЫ ---

    public List<Review> getAllForAdmin() {
        return reviewRepository.findAllByOrderByReviewDateDesc();
    }

    @Transactional
    public void approveReview(String id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        review.setModerated(true);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(String id) {
        if (!reviewRepository.existsById(id)) {
            throw new RuntimeException("Отзыв не найден");
        }
        reviewRepository.deleteById(id);
    }

    // Вспомогательный метод маппинга
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setReviewDate(review.getReviewDate());
        dto.setProductId(review.getProductId());

        if (review.getClient() != null && review.getClient().getFirstName() != null) {
            dto.setClientName(review.getClient().getFirstName());
        } else {
            dto.setClientName("Гость");
        }
        return dto;
    }
}