package org.diplom_back.modules.products.repository;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByIsActiveTrue();

}