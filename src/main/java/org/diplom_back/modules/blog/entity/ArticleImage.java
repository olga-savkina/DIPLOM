package org.diplom_back.modules.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "article_image")
@Data
public class ArticleImage {
    @Id
    @Column(name = "image_id", length = 36)
    private String imageId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    private boolean isMain;

    @ManyToOne
    @JoinColumn(name = "article_id")
    @JsonIgnore
    private Article article;
}