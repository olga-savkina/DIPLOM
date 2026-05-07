package org.diplom_back.modules.orders.service;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.*;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.orders.repository.*;
import org.diplom_back.modules.products.entity.ProductVariant;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    // 1. Все репозитории объявляем один раз в начале
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Создание нового заказа (использует OrderRequest)
     */
    @Transactional
    public Order createOrder(OrderRequest dto, User user) {
        Client client = user.getClient();

        Order order = new Order();
        order.setOrderId(UUID.randomUUID().toString());
        order.setClient(client);
        order.setOrderDate(LocalDateTime.now());

        // --- ИЗМЕНЕНИЕ: Берем статус из DTO (который прислал фронтенд) ---
        // Если на фронте выбрали CARD -> придет PAID, если CASH -> придет PENDING
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");

        order.setShippingAddress(dto.getShippingAddress());
        order.setPaymentMethod(dto.getPaymentMethod()); // Не забудь сохранить способ оплаты

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        // --- ЛОГИКА ТОВАРОВ ---
        for (CartItemDTO itemDto : dto.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemDto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден: " + itemDto.getVariantId()));

            if (variant.getStockQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Недостаточно товара на складе: " + variant.getProduct().getName());
            }

            variant.setStockQuantity(variant.getStockQuantity() - itemDto.getQuantity());
            productVariantRepository.save(variant);

            OrderItem item = new OrderItem();
            item.setOrderItemId(UUID.randomUUID().toString());
            item.setOrder(order);
            item.setVariantId(itemDto.getVariantId());
            item.setQuantity(itemDto.getQuantity());
            item.setPriceAtSale(itemDto.getPrice());

            items.add(item);
            total = total.add(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        // --- ЛОГИКА БОНУСОВ ---

        // 1. Списание бонусов (если фронтенд передал usedBonuses)
        if (dto.getUsedBonuses() != null && dto.getUsedBonuses().compareTo(BigDecimal.ZERO) > 0) {
            // Проверяем, не пытается ли юзер списать больше, чем у него есть
            if (client.getBonusPoints() < dto.getUsedBonuses().intValue()) {
                throw new RuntimeException("Недостаточно бонусов для списания");
            }
            // Уменьшаем баланс клиента
            client.setBonusPoints(client.getBonusPoints() - dto.getUsedBonuses().intValue());
            // Вычитаем бонусы из итоговой суммы заказа
            total = total.subtract(dto.getUsedBonuses());
        }

        order.setTotalAmount(total);
        order.setItems(items);

        // 2. Начисление новых бонусов (10% от фактически оплаченной суммы)
        int bonusEarned = total.multiply(new BigDecimal("0.1")).intValue();
        client.setBonusPoints(client.getBonusPoints() + bonusEarned);

        // Сохраняем клиента один раз
        clientRepository.save(client);

        return orderRepository.save(order);
    }

    /**
     * Получение истории заказов (использует OrderResponseDTO для фронтенда)
     */
    public List<OrderResponseDTO> getUserOrders(String email) {
        // Получаем заказы из базы и конвертируем их, обогащая названиями товаров
        return orderRepository.findByClientUserEmail(email).stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    /**
     * Вспомогательный метод для превращения Entity в DTO с названием товара
     */
    public OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate().toString());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());

        // --- ДОБАВЬТЕ ЭТОТ БЛОК ---
        if (order.getClient() != null) {
            // Создаем DTO для клиента, чтобы передать имя и телефон
            ClientResponseDTO clientDto = new ClientResponseDTO();
            clientDto.setFirstName(order.getClient().getFirstName());
            clientDto.setLastName(order.getClient().getLastName());
            clientDto.setPhoneNumber(order.getClient().getPhoneNumber());
            dto.setClient(clientDto);
        }
        // --------------------------

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

    @Transactional
    public void cancelOrder(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        // Проверка владельца заказа
        if (!order.getClient().getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Вы не можете отменить чужой заказ");
        }

        // Проверка возможности отмены
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Нельзя отменить заказ в статусе: " + order.getStatus());
        }

        // 1. Возврат товара на склад
        for (OrderItem item : order.getItems()) {
            productVariantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                productVariantRepository.save(variant);
            });
        }

        // 2. Смена статуса
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
}