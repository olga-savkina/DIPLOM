package org.diplom_back.modules.products.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "review")
@Data
public class Review {
    @Id
    @Column(name = "review_id")
    private String reviewId = UUID.randomUUID().toString();

    @Column(name = "product_id")
    private String productId;

    @Column(name = "client_id")
    private String clientId;

    private int rating;
    private String comment;

    @Column(name = "review_date")
    private LocalDateTime reviewDate = LocalDateTime.now();

    @Column(name = "is_moderated")
    private boolean isModerated = false;
}