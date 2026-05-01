package org.diplom_back.modules.orders.repository;

import org.diplom_back.modules.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {}
