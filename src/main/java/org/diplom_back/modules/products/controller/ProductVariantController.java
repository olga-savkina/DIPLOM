package org.diplom_back.modules.products.controller;

import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/variants")
public class ProductVariantController {

    @Autowired
    private ProductVariantService variantService;

    // Получить все размеры товара
    @GetMapping("/product/{productId}")
    public List<ProductVariant> getVariants(@PathVariable String productId) {
        return variantService.getVariantsByProductId(productId);
    }

    // Добавить новый размер/цвет к товару
    @PostMapping("/product/{productId}")
    public ResponseEntity<ProductVariant> addVariant(
            @PathVariable String productId,
            @RequestBody ProductVariant variant) {
        return ResponseEntity.ok(variantService.addVariant(productId, variant));
    }

    // Быстрое обновление только количества
    @PatchMapping("/{variantId}/stock")
    public ResponseEntity<?> updateStock(
            @PathVariable String variantId,
            @RequestParam Integer quantity) {
        variantService.updateStock(variantId, quantity);
        return ResponseEntity.ok("Количество обновлено");
    }

    // Удалить вариант (размер)
    @DeleteMapping("/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable String variantId) {
        variantService.deleteVariant(variantId);
        return ResponseEntity.ok("Вариант удален");
    }
}
