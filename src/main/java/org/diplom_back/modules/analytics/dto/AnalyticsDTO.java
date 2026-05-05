package org.diplom_back.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AnalyticsDTO {
    private double totalRevenue;
    private long totalOrders;
    private double averageCheck;
    private List<ProductSalesDTO> topProducts;
    private Map<String, Double> salesDynamics; // Дата -> Выручка
}


