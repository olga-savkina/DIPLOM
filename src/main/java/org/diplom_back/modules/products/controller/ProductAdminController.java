package org.diplom_back.modules.products.controller;

import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductImage;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductImageRepository;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.products.service.ProductService;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
public class ProductAdminController {
    @Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

    // Добавление нового товара
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("product") Product product,
            @RequestPart("images") MultipartFile[] images) {
        try {
            Product saved = productService.addProduct(product, images);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при сохранении файлов");
        }
    }

    // Добавление фото к существующему товару
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addImagesToProduct(
            @PathVariable String id,
            @RequestParam("images") MultipartFile[] files) {
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            productService.saveProductImages(product, files); // Используем метод из сервиса
            return ResponseEntity.ok("Фотографии добавлены");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка загрузки");
        }
    }

    @GetMapping
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();

        // Проходим циклом по всем продуктам
        for (Product product : products) {
            // Внутри каждого продукта проходим по его вариантам
            if (product.getVariants() != null) {
                for (ProductVariant variant : product.getVariants()) {
                    // Догружаем сток для каждого варианта
                    warehouseStockRepository.findByVariant_VariantId(variant.getVariantId())
                            .ifPresent(variant::setStock);
                }
            }
        }

        return products;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok("Товар удален");
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable String id,
            @RequestPart("product") Product details, // Изменено с @RequestBody на @RequestPart
            @RequestPart(value = "images", required = false) MultipartFile[] images) { // Добавлен прием файлов
        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            // Обновляем текстовые данные
            product.setName(details.getName());
            product.setBrand(details.getBrand());
            product.setBasePrice(details.getBasePrice());
            product.setDescription(details.getDescription());
            product.setActive(details.isActive());
            product.setCategories(details.getCategories());

            // Сохраняем основные данные товара
            productRepository.save(product);

            // Если прикреплены новые изображения — сохраняем их
            if (images != null && images.length > 0) {
                productService.saveProductImages(product, images);
            }

            return ResponseEntity.ok("Данные и изображения обновлены");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка при сохранении новых изображений");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка обновления: " + e.getMessage());
        }
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable String productId, @PathVariable String imageId) {
        try {
            // Проверяем, существует ли запись, прежде чем удалять
            if (productImageRepository.existsById(imageId)) {
                productImageRepository.deleteById(imageId); // Обращаемся к переменной бина
                return ResponseEntity.ok("Картинка удалена");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Картинка не найдена");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении");
        }
    }

}