package org.diplom_back.modules.blog.repository;

import org.diplom_back.modules.blog.entity.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticleImageRepository extends JpaRepository<ArticleImage, String> {

    // Найти все картинки к конкретной статье
    List<ArticleImage> findByArticle_ArticleId(String articleId);

    // Удалить все картинки статьи (полезно при полной очистке галереи)
    void deleteByArticle_ArticleId(String articleId);
}