package org.diplom_back.modules.warehouse.repository;

import jakarta.transaction.Transactional;
import org.diplom_back.modules.products.entity.Product;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, String> {

    // Поиск запаса по ID конкретной модификации товара
   // WarehouseStock findByVariantId(String variantId);

    @Modifying
    @Transactional
    @Query("UPDATE WarehouseStock w SET w.quantity = w.quantity + :amount WHERE w.variant.variantId = :variantId")
    void addStock(@Param("variantId") String variantId, @Param("amount") Integer amount);

    Optional<WarehouseStock> findByVariant_VariantId(String variantId);

}
