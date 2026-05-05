package org.diplom_back.modules.analytics.service;

import org.diplom_back.modules.analytics.dto.AnalyticsDTO;
import org.diplom_back.modules.analytics.dto.ProductSalesDTO; // Импорт DTO
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem; // Импорт сущности OrderItem
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductVariantRepository variantRepository;

    public AnalyticsDTO getStatistics() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Считаем общую выручку
        double totalRevenue = allOrders.stream()
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();

        // 2. Считаем количество заказов и средний чек
        long totalOrders = allOrders.size();
        double avgCheck = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // 3. Группировка продаж по дням для графика (Динамика)
        Map<String, Double> dynamics = allOrders.stream()
                .filter(o -> o.getOrderDate() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate().toString(),
                        Collectors.summingDouble(o -> o.getTotalAmount().doubleValue())
                ));

        // 4. Группировка продаж по ТОВАРУ (а не по варианту)
        Map<String, Long> productCounts = allOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(item -> {
                    // Идем по цепочке: Item -> Variant -> Product
                    ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
                    if (variant != null && variant.getProduct() != null) {
                        return variant.getProduct().getName(); // Берем имя самого товара
                    }
                    return "Неизвестный товар";
                })
                .collect(Collectors.groupingBy(
                        name -> name,
                        Collectors.summingLong(name -> 1L) // Или суммируйте item.getQuantity(), если нужно кол-во штук
                ));

        // Преобразуем в список ТОП-5
        List<ProductSalesDTO> topProducts = productCounts.entrySet().stream()
                .map(entry -> new ProductSalesDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(5)
                .collect(Collectors.toList());

        return new AnalyticsDTO(totalRevenue, totalOrders, avgCheck, topProducts, dynamics);
    }
}