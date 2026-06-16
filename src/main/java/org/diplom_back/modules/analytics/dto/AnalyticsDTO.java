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
    private Map<String, Long> cityStats;      // География: "Город" -> Количество
    private Map<String, Long> cohortData;     // Когорты: "Месяц" -> Новых юзеров
    private double totalBonusesUsed;          // Сумма списанных бонусов
    private long ordersWithBonuses;           // Кол-во заказов со скидкой
    private long totalReviews;                // Всего отзывов
    private double avgRating;                 // Средний рейтинг (0.0 - 5.0)
    private long totalUsers;                  // Всего человек в системе
    private long activeCustomers;             // Сколько из них реально покупали
    private List<AbcXyzProductDTO> abcXyzMatrix;
}


