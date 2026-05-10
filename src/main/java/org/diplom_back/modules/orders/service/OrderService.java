package org.diplom_back.modules.orders.service;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.*;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.orders.entity.OrderStatus;
import org.diplom_back.modules.orders.repository.*;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.diplom_back.modules.warehouse.repository.WarehouseStockRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WarehouseStockRepository stockRepository;

    /**
     * Создание заказа: РЕЗЕРВИРОВАНИЕ ТОВАРА
     */
    @Transactional
    public Order createOrder(OrderRequest dto, User user) {
        Client client = user.getClient();
        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());

        // Установка статуса
        order.setStatus(dto.getStatus() != null ? OrderStatus.valueOf(dto.getStatus()) : OrderStatus.PENDING);
        order.setShippingAddress(dto.getShippingAddress());
        order.setPaymentMethod(dto.getPaymentMethod());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        // Обработка товаров на складе
        for (CartItemDTO itemDto : dto.getItems()) {
            WarehouseStock stock = stockRepository.findByVariant_VariantId(itemDto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Складская запись не найдена для: " + itemDto.getVariantId()));

            int available = stock.getQuantity() - stock.getReservedQuantity();
            if (available < itemDto.getQuantity()) {
                throw new RuntimeException("Недостаточно свободного товара: " + itemDto.getVariantId());
            }

            stock.setReservedQuantity(stock.getReservedQuantity() + itemDto.getQuantity());
            stockRepository.save(stock);

            OrderItem item = new OrderItem();
            item.setOrderItemId(UUID.randomUUID().toString());
            item.setOrder(order);
            item.setVariantId(itemDto.getVariantId());
            item.setQuantity(itemDto.getQuantity());
            item.setPriceAtSale(itemDto.getPrice());

            items.add(item);
            subtotal = subtotal.add(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        // --- ЛОГИКА СКИДОК (Первый заказ / День рождения) ---
        BigDecimal discountMultiplier = BigDecimal.ZERO;

        long orderCount = orderRepository.countByClient_User(user);
        if (orderCount == 0) {
            discountMultiplier = new BigDecimal("0.20");
        } else {
            LocalDate today = LocalDate.now();
            boolean isBirthdayPeriod = user.getChildren().stream().anyMatch(child -> {
                if (child.getBirthDate() == null) return false;
                LocalDate bdayThisYear = child.getBirthDate().withYear(today.getYear());
                return !today.isBefore(bdayThisYear.minusDays(3)) && !today.isAfter(bdayThisYear.plusDays(3));
            });
            if (isBirthdayPeriod) {
                discountMultiplier = new BigDecimal("0.20");
            }
        }

        BigDecimal discountAmount = subtotal.multiply(discountMultiplier);
        BigDecimal amountAfterDiscount = subtotal.subtract(discountAmount);

        // --- ЛОГИКА БОНУСОВ ---
        BigDecimal finalTotal = amountAfterDiscount;

        if (dto.getUsedBonuses() != null && dto.getUsedBonuses().compareTo(BigDecimal.ZERO) > 0) {
            int bonusesToUse = dto.getUsedBonuses().intValue();

            if (client.getBonusPoints() < bonusesToUse) {
                throw new RuntimeException("Недостаточно бонусов на счету клиента");
            }

            // 1. Записываем использованные бонусы в объект заказа (ДЛЯ БД)
            order.setUsedBonuses(bonusesToUse);

            // 2. Списываем бонусы с баланса клиента
            client.setBonusPoints(client.getBonusPoints() - bonusesToUse);

            // 3. Вычитаем из итоговой суммы оплаты
            finalTotal = finalTotal.subtract(dto.getUsedBonuses());
        } else {
            order.setUsedBonuses(0);
        }

        // Проверка, чтобы сумма не стала отрицательной
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) finalTotal = BigDecimal.ZERO;

        order.setTotalAmount(finalTotal);
        order.setItems(items);

        // --- НАЧИСЛЕНИЕ НОВОГО КЕШБЭКА (10% от того, что реально оплатили) ---
        int bonusEarned = finalTotal.multiply(new BigDecimal("0.1")).intValue();
        client.setBonusPoints(client.getBonusPoints() + bonusEarned);

        // Сохраняем клиента и заказ
        clientRepository.save(client);
        return orderRepository.save(order); // Исправлена опечатка ordeуrRepository
    }

    /**
     * Отмена заказа: СНЯТИЕ РЕЗЕРВА
     */
    @Transactional
    public void cancelOrder(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        if (!order.getClient().getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        // Отменить можно только то, что еще не уехало к клиенту
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Заказ нельзя отменить в текущем статусе");
        }

        // ШАГ 2: Уменьшаем резерв (товар снова становится доступным)
        for (OrderItem item : order.getItems()) {
            stockRepository.findByVariant_VariantId(item.getVariantId()).ifPresent(stock -> {
                stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());
                stockRepository.save(stock);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    /**
     * Завершение заказа (Админский метод): ФИЗИЧЕСКОЕ СПИСАНИЕ
     * Вызывать, когда админ меняет статус на "Выполнено" или "Доставлено"
     */
    @Transactional
    public void completeOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        for (OrderItem item : order.getItems()) {
            stockRepository.findByVariant_VariantId(item.getVariantId()).ifPresent(stock -> {
                // ШАГ 3: Вычитаем и из общего количества, и из резерва
                stock.setQuantity(stock.getQuantity() - item.getQuantity());
                stock.setReservedQuantity(stock.getReservedQuantity() - item.getQuantity());
                stockRepository.save(stock);
            });
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }

    public List<OrderResponseDTO> getUserOrders(String email) {
        return orderRepository.findByClientUserEmail(email).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate().toString());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setShippingAddress(order.getShippingAddress());

        if (order.getClient() != null) {
            ClientResponseDTO clientDto = new ClientResponseDTO();
            clientDto.setFirstName(order.getClient().getFirstName());
            clientDto.setLastName(order.getClient().getLastName());
            clientDto.setPhoneNumber(order.getClient().getPhoneNumber());
            dto.setClient(clientDto);
        }

        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
                    itemDto.setVariantId(item.getVariantId());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPriceAtSale(item.getPriceAtSale());

                    productVariantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                        itemDto.setProductName(variant.getProduct().getName());
                        itemDto.setColor(variant.getColor());
                        itemDto.setSize(variant.getSize());
                        itemDto.setSku(variant.getSku());
                    });
                    return itemDto;
                }).toList();

        dto.setItems(itemDTOs);
        return dto;
    }
}