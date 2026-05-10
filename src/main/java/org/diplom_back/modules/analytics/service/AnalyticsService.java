package org.diplom_back.modules.analytics.service;

import org.diplom_back.modules.analytics.dto.AnalyticsDTO;
import org.diplom_back.modules.analytics.dto.ProductSalesDTO;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.diplom_back.modules.products.entity.*;
import org.diplom_back.modules.products.repository.*;
import org.diplom_back.modules.auth.repository.UserRepository; // Репозиторий пользователей
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductVariantRepository variantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public AnalyticsDTO getStatistics() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Основные метрики: Выручка, Заказы, Средний чек
        double totalRevenue = allOrders.stream()
                .mapToDouble(o -> o.getTotalAmount().doubleValue())
                .sum();
        long totalOrders = allOrders.size();
        double avgCheck = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // 2. Динамика продаж
        Map<String, Double> dynamics = allOrders.stream()
                .filter(o -> o.getOrderDate() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate().toString(),
                        Collectors.summingDouble(o -> o.getTotalAmount().doubleValue())
                ));

        // 3. ТОП-5 товаров
        Map<String, Long> productCounts = allOrders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(item -> {
                    ProductVariant variant = variantRepository.findById(item.getVariantId()).orElse(null);
                    return (variant != null && variant.getProduct() != null) ? variant.getProduct().getName() : "Неизвестный товар";
                })
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        List<ProductSalesDTO> topProducts = productCounts.entrySet().stream()
                .map(entry -> new ProductSalesDTO(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .limit(5)
                .collect(Collectors.toList());

        // --- НОВЫЕ МЕТРИКИ ---

        // 4. География (извлекаем первое слово из адреса до запятой)
        Map<String, Long> cityStats = allOrders.stream()
                .filter(o -> o.getShippingAddress() != null && !o.getShippingAddress().isBlank())
                .map(o -> {
                    String fullAddress = o.getShippingAddress().trim();
                    // Берем первое слово до запятой или пробела и делаем первую букву заглавной
                    String city = fullAddress.split("[,\\s]+")[0];
                    return city.substring(0, 1).toUpperCase() + city.substring(1).toLowerCase();
                })
                .collect(Collectors.groupingBy(city -> city, Collectors.counting()));

        // 5. Когортный анализ (новые пользователи по месяцам регистрации)
        Map<String, Long> cohortData = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().getMonth().getDisplayName(TextStyle.FULL, new Locale("ru")),
                        Collectors.counting()
                ));

        // 6. Применение бонусов
        // Считаем общее количество использованных бонусов во всех заказах
        double totalBonusesUsed = allOrders.stream()
                .filter(o -> o.getUsedBonuses() != null)
                .mapToDouble(o -> o.getUsedBonuses().doubleValue())
                .sum();

        // Процент заказов, где были применены бонусы
        long ordersWithBonuses = allOrders.stream()
                .filter(o -> o.getUsedBonuses() != null && o.getUsedBonuses().doubleValue() > 0)
                .count();

        // 7. Отзывы и рейтинг
        List<Review> allReviews = reviewRepository.findAll();
        long totalReviews = allReviews.size();
        double avgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // Статистика пользователей
        // 8. ПОЛЬЗОВАТЕЛИ (Используем поле userId из класса User)
        long totalUsers = userRepository.count();

        long activeCustomers = allOrders.stream()
                .filter(o -> o.getClient() != null && o.getClient().getUser() != null)
                .map(o -> o.getClient().getUser().getUserId()) // Вызываем getUserId() у объекта User
                .distinct()
                .count();

        // Не забудь обновить AnalyticsDTO, чтобы он принимал все эти поля в конструктор
        return new AnalyticsDTO(
                totalRevenue, totalOrders, avgCheck, topProducts, dynamics,
                cityStats, cohortData, totalBonusesUsed, ordersWithBonuses,
                totalReviews, avgRating, totalUsers, activeCustomers
        );
    }
}