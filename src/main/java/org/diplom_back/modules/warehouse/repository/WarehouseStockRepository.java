package org.diplom_back.modules.warehouse.repository;

import org.diplom_back.modules.warehouse.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, String> {

    // Поиск запаса по ID конкретной модификации товара
    WarehouseStock findByVariantId(String variantId);

    // Тот самый метод для быстрой закупки (добавления количества)
    @Transactional
    @Modifying
    @Query("UPDATE WarehouseStock w SET w.quantity = w.quantity + :amount WHERE w.variantId = :variantId")
    void addStock(String variantId, Integer amount);
}
