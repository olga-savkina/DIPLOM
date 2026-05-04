package org.diplom_back.modules.blog.service;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.blog.entity.*;
import org.diplom_back.modules.products.entity.Category;
import org.diplom_back.modules.blog.repository.*;
import org.diplom_back.modules.products.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleImageRepository articleImageRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private final String UPLOAD_PATH = "F:/будущийдиплом/DIPLOM_PROGA/diplom_uploads";

    @Transactional
    public Article addArticle(Article article, MultipartFile[] images) throws IOException {
        article.setArticleId(UUID.randomUUID().toString());
        article.setPublicationDate(LocalDateTime.now());

        linkCategory(article);

        Article savedArticle = articleRepository.save(article);
        saveArticleImages(savedArticle, images);
        return savedArticle;
    }

    @Transactional
    public void updateArticle(String id, Article details) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Статья не найдена"));

        article.setTitle(details.getTitle());
        article.setContent(details.getContent());

        // Обновляем категорию
        article.setCategory(details.getCategory());
        linkCategory(article);

        articleRepository.save(article);
    }

    @Transactional
    public void deleteArticleImage(String imageId) throws IOException {
        ArticleImage image = articleImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Изображение не найдено"));

        // Удаляем физический файл (опционально, но правильно для 3NF проекта)
        // Извлекаем имя файла из URL /uploads/uuid_name.jpg
        String fileName = image.getImageUrl().replace("/uploads/", "");
        Path filePath = Paths.get(UPLOAD_PATH, fileName);
        Files.deleteIfExists(filePath);

        articleImageRepository.delete(image);
    }

    @Transactional
    public void saveArticleImages(Article article, MultipartFile[] images) throws IOException {
        if (images == null || images.length == 0) return;

        File uploadDir = new File(UPLOAD_PATH);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        for (MultipartFile file : images) {
            if (file != null && !file.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get(UPLOAD_PATH, fileName);
                Files.copy(file.getInputStream(), filePath);

                ArticleImage image = new ArticleImage();
                image.setImageId(UUID.randomUUID().toString());
                image.setArticle(article);
                image.setImageUrl("/uploads/" + fileName);
                articleImageRepository.save(image);
            }
        }
    }

    private void linkCategory(Article article) {
        if (article.getCategory() != null && article.getCategory().getCategoryId() != null) {
            Category existingCategory = categoryRepository.findById(article.getCategory().getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            article.setCategory(existingCategory);
        }
    }
}