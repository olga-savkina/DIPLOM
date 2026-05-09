package org.diplom_back.modules.warehouse.service;

import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.diplom_back.modules.warehouse.dto.CategoryDTO;
import org.diplom_back.modules.warehouse.dto.ImageDTO;
import org.diplom_back.modules.warehouse.dto.ProductWarehouseDTO;
import org.diplom_back.modules.warehouse.dto.VariantWarehouseDTO;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseStockRepository warehouseStockRepository;

    @Autowired
    private  ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<ProductWarehouseDTO> getAllInventory() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            ProductWarehouseDTO dto = new ProductWarehouseDTO();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setBrand(product.getBrand());
            dto.setDescription(product.getDescription());
            dto.setBasePrice(product.getBasePrice());

            // Категории
            dto.setCategories(product.getCategories().stream().map(cat -> {
                CategoryDTO c = new CategoryDTO();
                c.setCategoryId(cat.getCategoryId());
                c.setName(cat.getName());
                return c;
            }).collect(Collectors.toList()));

            // Картинки
            dto.setImages(product.getImages().stream().map(img -> {
                ImageDTO i = new ImageDTO();
                i.setImageId(img.getImageId());
                i.setImageUrl(img.getImageUrl());
                return i;
            }).collect(Collectors.toList()));

            // Варианты + Сток (через связь в Entity)
            dto.setVariants(product.getVariants().stream().map(variant -> {
                VariantWarehouseDTO vDto = new VariantWarehouseDTO();
                vDto.setVariantId(variant.getVariantId());
                vDto.setSku(variant.getSku());
                vDto.setSize(variant.getSize());
                vDto.setColor(variant.getColor());
                vDto.setPriceOverride(variant.getPriceOverride());
                vDto.setAgeMin(variant.getAgeMin());
                vDto.setAgeMax(variant.getAgeMax());

                // Берем сток напрямую из объекта варианта
                WarehouseStock stock = variant.getStock();
                if (stock != null) {
                    vDto.setQuantity(stock.getQuantity());
                    vDto.setReservedQuantity(stock.getReservedQuantity());
                    vDto.setExpiryDate(stock.getExpiryDate()); // Теперь даты в стоке
                    vDto.setProductionDate(stock.getProductionDate());
                    vDto.setLastUpdated(stock.getLastUpdated());
                } else {
                    vDto.setQuantity(0);
                    vDto.setReservedQuantity(0);
                }

                return vDto;
            }).collect(Collectors.toList()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void replenishStock(String variantId, Integer amount, LocalDate expiryDate) {
        WarehouseStock stock = warehouseStockRepository.findByVariant_VariantId(variantId)
                .orElseGet(() -> {
                    WarehouseStock newStock = new WarehouseStock();
                    newStock.setStockId(java.util.UUID.randomUUID().toString()); // Генерируем ID для новой записи

                    // 1. Сначала находим сам объект варианта в базе
                    // Тебе нужно внедрить productVariantRepository в этот сервис через @Autowired
                    ProductVariant variantObj = productVariantRepository.findById(variantId)
                            .orElseThrow(() -> new RuntimeException("Вариант товара с ID " + variantId + " не найден"));

                    // 2. Теперь устанавливаем этот объект в новую запись склада
                    newStock.setVariant(variantObj);
                    newStock.setQuantity(0);
                    newStock.setReservedQuantity(0);
                    return newStock;
                });

        // 3. Обновляем данные
        stock.setQuantity(stock.getQuantity() + amount);
        stock.setExpiryDate(expiryDate);

        warehouseStockRepository.save(stock);
    }
    @Transactional
    public void finalizeStockRemoval(Order order) {
        for (OrderItem item : order.getItems()) {
            // Ищем запись на складе по variantId из позиции заказа
            WarehouseStock stock = warehouseStockRepository.findByVariant_VariantId(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Складская запись не найдена для варианта: " + item.getVariantId()));

            // Списываем физическое количество и убираем из резерва
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());

            warehouseStockRepository.save(stock);
        }
    }
}
