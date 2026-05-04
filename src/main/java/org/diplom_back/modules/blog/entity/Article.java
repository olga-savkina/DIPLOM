package org.diplom_back.modules.blog.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.diplom_back.modules.products.entity.Category;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "article")
@Data
public class Article {
    @Id
    @Column(name = "article_id", length = 36)
    private String articleId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime publicationDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleImage> images = new ArrayList<>();
}