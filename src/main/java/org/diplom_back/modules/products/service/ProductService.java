package org.diplom_back.modules.products.service;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.products.entity.Category;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductImage;
import org.diplom_back.modules.products.repository.CategoryRepository;
import org.diplom_back.modules.products.repository.ProductImageRepository;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
    public class ProductService {

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private ProductImageRepository productImageRepository;

        // Добавь репозиторий категорий!
        @Autowired
        private CategoryRepository categoryRepository;

        private final String UPLOAD_PATH = "F:/будущийдиплом/DIPLOM_PROGA/diplom_uploads";

        @Transactional
        public Product addProduct(Product product, MultipartFile[] images) throws IOException {
            // 1. Генерируем ID для нового товара
            product.setProductId(UUID.randomUUID().toString());

            // 2. ВАЖНО: Обработка категорий Many-to-Many
            // Предположим, у тебя в Product.java есть поле Set<Category> categories
            // и метод getCategoryIds() или categories уже содержит ID
            if (product.getCategories() != null) {
                List<String> ids = product.getCategories().stream()
                        .map(Category::getCategoryId)
                        .collect(Collectors.toList());

                List<Category> existingCategories = categoryRepository.findAllById(ids);
                // ВРЕМЕННО ДЛЯ ТЕСТА:
                System.out.println("Пришло категорий: " + (product.getCategories() != null ? product.getCategories().size() : 0));
                product.setCategories(existingCategories);
            }

            // 3. Сохраняем товар (теперь Hibernate создаст записи в связующей таблице)
            Product savedProduct = productRepository.save(product);

            // 4. Сохраняем фото
            saveProductImages(savedProduct, images);

            return savedProduct;
        }

        @Transactional
        public void saveProductImages(Product product, MultipartFile[] images) throws IOException {
            if (images == null || images.length == 0) return;

            File uploadDir = new File(UPLOAD_PATH);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(UPLOAD_PATH, fileName); // Используем прямой путь
                    Files.copy(file.getInputStream(), filePath);

                    ProductImage image = new ProductImage();
                    image.setImageId(UUID.randomUUID().toString());
                    image.setProduct(product);
                    image.setImageUrl("/uploads/" + fileName);
                    productImageRepository.save(image);
                }
            }
        }
    }