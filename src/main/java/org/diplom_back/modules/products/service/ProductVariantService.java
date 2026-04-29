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
        return variantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(String variantId) {
        variantRepository.deleteById(variantId);
    }

    @Transactional
    public ProductVariant updateStock(String variantId, Integer newQuantity) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Вариант не найден"));
        variant.setStockQuantity(newQuantity);
        return variantRepository.save(variant);
    }
}
