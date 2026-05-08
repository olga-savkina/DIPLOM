package org.diplom_back.modules.warehouse.service;

import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.products.repository.ProductRepository;
import org.diplom_back.modules.warehouse.dto.CategoryDTO;
import org.diplom_back.modules.warehouse.dto.ImageDTO;
import org.diplom_back.modules.warehouse.dto.ProductWarehouseDTO;
import org.diplom_back.modules.warehouse.dto.VariantWarehouseDTO;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseStockRepository stockRepository;

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

            // Маппим картинки
            dto.setImages(product.getImages().stream().map(img -> {
                ImageDTO i = new ImageDTO();
                i.setImageId(img.getImageId());
                i.setImageUrl(img.getImageUrl());
                return i;
            }).collect(Collectors.toList()));

            // Маппим категории
            dto.setCategories(product.getCategories().stream().map(cat -> {
                CategoryDTO c = new CategoryDTO();
                c.setCategoryId(cat.getCategoryId());
                c.setName(cat.getName());
                return c;
            }).collect(Collectors.toList()));

            // Маппим варианты с данными из СТОКА
            dto.setVariants(product.getVariants().stream().map(variant -> {
                VariantWarehouseDTO vDto = new VariantWarehouseDTO();
                vDto.setVariantId(variant.getVariantId());
                vDto.setSku(variant.getSku());
                vDto.setSize(variant.getSize());
                vDto.setColor(variant.getColor());
                vDto.setPriceOverride(variant.getPriceOverride());
                vDto.setAgeMin(variant.getAgeMin());
                vDto.setAgeMax(variant.getAgeMax());

                // Самый важный момент: берем данные из репозитория склада
                WarehouseStock stock = stockRepository.findByVariantId(variant.getVariantId());
                if (stock != null) {
                    vDto.setQuantity(stock.getQuantity());
                    vDto.setReservedQuantity(stock.getReservedQuantity());
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
    public void replenishStock(String variantId, Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Количество для закупки должно быть больше нуля");
        }

        // Используем метод из репозитория, который мы создали ранее
        stockRepository.addStock(variantId, amount);

        // Здесь можно добавить логику логирования: кто и когда закупил товар
    }
}
