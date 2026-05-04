package org.diplom_back.modules.blog.repository;

import org.diplom_back.modules.blog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, String> {

    // Поиск всех статей конкретной категории (например, только "Акции")
    List<Article> findByCategory_CategoryIdOrderByPublicationDateDesc(String categoryId);

    // Поиск самых свежих статей для главной страницы
    List<Article> findTop3ByOrderByPublicationDateDesc();
}
