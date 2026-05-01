package org.diplom_back.modules.orders.service;

import lombok.RequiredArgsConstructor;
import org.diplom_back.modules.auth.entity.Client;
import org.diplom_back.modules.auth.entity.User;
import org.diplom_back.modules.auth.repository.*;
import org.diplom_back.modules.orders.dto.*;
import org.diplom_back.modules.orders.entity.Order;
import org.diplom_back.modules.orders.entity.OrderItem;
import org.diplom_back.modules.orders.repository.*;
import org.diplom_back.modules.products.repository.ProductVariantRepository;
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
        order.setStatus("PENDING");
        order.setShippingAddress(dto.getShippingAddress());

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CartItemDTO itemDto : dto.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrderItemId(UUID.randomUUID().toString());
            item.setOrder(order);
            item.setVariantId(itemDto.getVariantId());
            item.setQuantity(itemDto.getQuantity());
            item.setPriceAtSale(itemDto.getPrice());

            items.add(item);
            total = total.add(itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
        }

        order.setTotalAmount(total);
        order.setItems(items);

        // Логика бонусов
        int bonus = total.multiply(new BigDecimal("0.1")).intValue();
        client.setBonusPoints(client.getBonusPoints() + bonus);
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
    private OrderResponseDTO convertToResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate().toString());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());

        List<OrderItemResponseDTO> itemDTOs = order.getItems().stream()
                .map(item -> {
                    OrderItemResponseDTO itemDto = new OrderItemResponseDTO();
                    itemDto.setVariantId(item.getVariantId());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setPriceAtSale(item.getPriceAtSale());

                    // Находим вариант и заполняем детальную информацию
                    productVariantRepository.findById(item.getVariantId()).ifPresent(variant -> {
                        itemDto.setProductName(variant.getProduct().getName()); // Из сущности Product
                        itemDto.setColor(variant.getColor()); // Из сущности ProductVariant
                        itemDto.setSize(variant.getSize());   // Из сущности ProductVariant
                        itemDto.setSku(variant.getSku());     // На всякий случай оставляем
                    });

                    return itemDto;
                }).toList();

        dto.setItems(itemDTOs);
        return dto;
    }
}