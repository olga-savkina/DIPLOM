package org.diplom_back.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductSalesDTO {
    private String name;
    private long count;
}
