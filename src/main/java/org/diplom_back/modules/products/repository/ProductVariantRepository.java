package org.diplom_back.modules.products.repository;

import org.diplom_back.modules.products.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    List<ProductVariant> findByProductProductId(String productId);
    default String findProductNameByVariantId(String variantId) {
        return findById(variantId)
                .map(variant -> variant.getProduct().getName())
                .orElse("Товар не найден");
    }
}