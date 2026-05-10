package org.diplom_back.modules.products.service;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private WarehouseStockRepository stockRepository;

    public List<ProductVariant> getVariantsByProductId(String productId) {
        return variantRepository.findByProductProductId(productId);
    }

    @Transactional
    public ProductVariant addVariant(String productId, ProductVariant variant) {
        // 1. Ищем продукт
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));

        // 2. Инициализируем вариант
        variant.setVariantId(UUID.randomUUID().toString());
        variant.setProduct(product);

        // Временно сохраняем объект стока, который пришел с фронта
        WarehouseStock incomingStock = variant.getStock();

        // ВАЖНО: обнуляем сток в объекте варианта перед первым сохранением,
        // чтобы избежать циклической ошибки или попытки сохранить пустой объект
        variant.setStock(null);

        // 3. СОХРАНЯЕМ ВАРИАНТ ПЕРВЫМ
        ProductVariant savedVariant = variantRepository.save(variant);

        // 4. ТЕПЕРЬ СОХРАНЯЕМ СКЛАД
        if (incomingStock != null) {
            incomingStock.setStockId(UUID.randomUUID().toString());
            incomingStock.setVariant(savedVariant); // Привязываем к уже сохраненному варианту
            incomingStock.setLastUpdated(LocalDateTime.now());
            incomingStock.setReservedQuantity(0);

            // Явное сохранение через репозиторий склада
            stockRepository.save(incomingStock);

            // Устанавливаем связь обратно для возвращаемого объекта
            savedVariant.setStock(incomingStock);
        }

        return savedVariant;
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