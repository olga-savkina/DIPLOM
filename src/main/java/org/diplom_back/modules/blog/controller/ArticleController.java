package org.diplom_back.modules.blog.controller;


import org.diplom_back.modules.blog.entity.Article;
import org.diplom_back.modules.blog.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles") // Публичный путь
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @GetMapping
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Article getArticle(@PathVariable String id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Статья не найдена"));
    }
}