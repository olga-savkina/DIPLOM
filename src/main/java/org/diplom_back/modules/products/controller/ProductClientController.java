package org.diplom_back.modules.products.controller;

import org.diplom_back.modules.products.entity.Category;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.CategoryRepository;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.util.List;

@RestController
@RequestMapping("/api/products")

public class ProductClientController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

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

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) throws Exception {
        Product product = productRepository.findById(id).orElseThrow();

        // Проходим по всем вариантам и вручную ищем для них сток в базе
        for (ProductVariant variant : product.getVariants()) {
            warehouseStockRepository.findByVariant_VariantId(variant.getVariantId())
                    .ifPresent(variant::setStock);
        }

        // Твой вывод в консоль для проверки
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        System.out.println(ow.writeValueAsString(product));

        return product;
    }
}