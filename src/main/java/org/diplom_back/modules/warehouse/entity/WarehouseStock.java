package org.diplom_back.modules.warehouse.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.diplom_back.modules.products.entity.ProductVariant;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_stock")
@Data
public class WarehouseStock {

    @Id
    @Column(name = "stock_id", length = 36)
    private String stockId;

    // УДАЛЕНО: private String variantId; (Это вызывало конфликт с @JoinColumn)

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    @OneToOne
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id")
    @JsonBackReference
    private ProductVariant variant;
}