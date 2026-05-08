package org.diplom_back.modules.products.service;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductVariant> getVariantsByProductId(String productId) {
        return variantRepository.findByProductProductId(productId);
    }

    @Transactional
    public ProductVariant addVariant(String productId, ProductVariant variant) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        variant.setVariantId(UUID.randomUUID().toString());
        variant.setProduct(product);

        // ВАЖНО: При создании нового варианта складская запись
        // обычно создается автоматически через WarehouseService или триггер,
        // либо здесь НЕ устанавливаются количество и даты.

        return variantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(String variantId) {
        variantRepository.deleteById(variantId);
    }

    @Transactional
    public ProductVariant updateVariant(String variantId, ProductVariant details) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Вариант не найден"));

        // Обновляем ТОЛЬКО характеристики описания
        variant.setSize(details.getSize());
        variant.setColor(details.getColor());
        variant.setSku(details.getSku());

        // Новые специфичные поля (цены и возраст)
        variant.setPriceOverride(details.getPriceOverride());
        variant.setAgeMin(details.getAgeMin());
        variant.setAgeMax(details.getAgeMax());

        // СТАТИСТИЧЕСКИЕ ПОЛЯ (quantity, dates) УДАЛЕНЫ ОТСЮДА,
        // так как они управляются через WarehouseService

        return variantRepository.save(variant);
    }
}