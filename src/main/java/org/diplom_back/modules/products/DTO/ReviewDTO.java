package org.diplom_back.modules.products.DTO;

import lombok.Data;
import java.time.LocalDateTime;

@Data // Если используешь Lombok, если нет — создай геттеры и сеттеры
public class ReviewDTO {
    private String reviewId;
    private String comment;
    private int rating;
    private LocalDateTime reviewDate;
    private String clientName; // Сюда мы положим имя вместо ID
    private String productId;
}