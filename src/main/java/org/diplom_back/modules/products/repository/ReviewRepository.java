package org.diplom_back.modules.products.repository;

import org.diplom_back.modules.products.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    // Получаем только прошедшие модерацию отзывы для конкретного товара
    List<Review> findByProductIdAndIsModeratedTrueOrderByReviewDateDesc(String productId);
}