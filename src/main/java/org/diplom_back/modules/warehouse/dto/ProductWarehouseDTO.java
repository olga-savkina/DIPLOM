package org.diplom_back.modules.warehouse.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class ProductWarehouseDTO {
    private String productId;
    private String name;
    private String brand;
    private String description;
    private BigDecimal basePrice;
    private List<ImageDTO> images;
    private List<CategoryDTO> categories;
    private List<VariantWarehouseDTO> variants;
}