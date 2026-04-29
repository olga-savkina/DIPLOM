package org.diplom_back.modules.products.controller;

import org.diplom_back.modules.products.entity.Category;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.repository.CategoryRepository;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")

public class ProductClientController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    // Весь список товаров
    @GetMapping
    public List<Product> getAllActiveProducts() {
        // Если в репозитории есть метод findByIsActiveTrue, лучше использовать его
        return productRepository.findAll();
    }

    // Список всех категорий для фильтра
    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    // Деталка товара
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return productRepository.findById(id).orElseThrow();
    }
}