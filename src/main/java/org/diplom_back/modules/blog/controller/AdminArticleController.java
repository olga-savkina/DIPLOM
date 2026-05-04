package org.diplom_back.modules.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.diplom_back.modules.blog.entity.Article;
import org.diplom_back.modules.blog.repository.ArticleRepository;
import org.diplom_back.modules.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
public class AdminArticleController {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleRepository articleRepository;

    // 1. Создание статьи
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createArticle(
            @RequestPart("article") String articleJson,
            @RequestPart(value = "images", required = false) MultipartFile[] images) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            Article article = objectMapper.readValue(articleJson, Article.class);

            return ResponseEntity.ok(articleService.addArticle(article, images));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при создании статьи");
        }
    }

    // 2. Обновление текстовых данных
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable String id, @RequestBody Article details) {
        articleService.updateArticle(id, details);
        return ResponseEntity.ok("Статья обновлена");
    }

    // 3. Добавление новых фото к существующей статье
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addImagesToArticle(
            @PathVariable String id,
            @RequestParam("images") MultipartFile[] images) {
        try {
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Статья не найдена"));
            articleService.saveArticleImages(article, images);
            return ResponseEntity.ok("Фотографии добавлены");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при загрузке фото");
        }
    }

    // 4. Удаление конкретной картинки
    @DeleteMapping("/{articleId}/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable String imageId) {
        try {
            articleService.deleteArticleImage(imageId);
            return ResponseEntity.ok("Картинка удалена");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении");
        }
    }

    @GetMapping
    public List<Article> getAll() {
        return articleRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable String id) {
        articleRepository.deleteById(id);
        return ResponseEntity.ok("Статья удалена");
    }
}