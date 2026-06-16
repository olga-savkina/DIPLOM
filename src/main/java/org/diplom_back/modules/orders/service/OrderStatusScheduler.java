package org.diplom_back.modules.orders.service;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.orders.entity.OrderStatus;
import org.diplom_back.modules.orders.repository.OrderRepository;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.diplom_back.modules.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStatusScheduler {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseStockRepository warehouseStockRepository;
    @Autowired
    private WarehouseService warehouseService;
    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Transactional
    public void finalizeStockRemoval(Order order) {
        for (OrderItem item : order.getItems()) {
            WarehouseStock stock = warehouseStockRepository.findByVariant_VariantId(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Складская запись не найдена"));

            // Уменьшаем общее количество и резерв на число купленных товаров
            stock.setQuantity(stock.getQuantity() - item.getQuantity());
            stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());

            warehouseStockRepository.save(stock);
        }
    }

    // Запускать каждые 3 часа (время в миллисекундах)
    @Scheduled(cron = "0 25 17 * * *")
    @Transactional
    public void autoUpdateOrderStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Если оплачен и прошло больше 24 часов -> "Отправлен"
        List<Order> paidOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.PAID, now.minusDays(1)
        );
        for (Order order : paidOrders) {
            order.setStatus(OrderStatus.SHIPPED);
            orderRepository.save(order);
        }

        // 2. Если в статусе "Отправлен" больше 3 дней -> "Доставлен" (или "Завершен")
        List<Order> shippedOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.SHIPPED, now.minusDays(3)
        );
        for (Order order : shippedOrders) {
            order.setStatus(OrderStatus.DELIVERED);
            warehouseService.finalizeStockRemoval(order); // Списываем со склада
            orderRepository.save(order);
        }

        System.out.println("Автоматическое обновление статусов завершено в " + now);
    }

    @Scheduled(cron = "0 0 1 * * *") // Запуск каждую ночь в 01:00
    public void applyExpiryDiscounts() {
        LocalDate warningDate = LocalDate.now().plusDays(7);

        // Находим все записи на складе, где срок годности подходит к концу
        List<WarehouseStock> expiringStocks = warehouseStockRepository.findByExpiryDateBefore(warningDate);

        for (WarehouseStock stock : expiringStocks) {
            ProductVariant variant = stock.getVariant();

            // Проверяем, что цена еще не снижена и есть базовый продукт
            if (variant.getPriceOverride() == null && variant.getProduct() != null) {

                // Если getBasePrice() уже BigDecimal, используем его напрямую.
                // Если это double, тогда оставляем BigDecimal.valueOf(...)
                BigDecimal basePrice = variant.getProduct().getBasePrice();

                // Считаем скидку 30% (умножаем на 0.7)
                BigDecimal discountFactor = new BigDecimal("0.7");
                BigDecimal discountedPrice = basePrice.multiply(discountFactor)
                        .setScale(2, RoundingMode.HALF_UP);

                variant.setPriceOverride(discountedPrice);
                productVariantRepository.save(variant);

                System.out.println("Авто-скидка: товар " + variant.getSku() + " теперь стоит " + discountedPrice);
            }
        }
    }
}
