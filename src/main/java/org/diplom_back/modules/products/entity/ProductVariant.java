package org.diplom_back.modules.products.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.diplom_back.modules.warehouse.entity.WarehouseStock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
public class ProductVariant {

    @Id
    private String variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    private String size;
    private String color;
    private Integer stockQuantity;
    private String sku;

    // --- НОВЫЕ ПОЛЯ ---

    @Column(name = "price_override")
    private BigDecimal priceOverride; // Индивидуальная цена для этого размера/веса

    @Column(name = "age_min")
    private Integer ageMin; // Мин. возраст в месяцах

    @Column(name = "age_max")
    private Integer ageMax; // Макс. возраст в месяцах

    @Column(name = "expiry_date")
    private LocalDate expiryDate; // Срок годности

    @Column(name = "production_date")
    private LocalDate productionDate; // Дата производства

    // --- МЕТОДЫ ---

    @PrePersist
    public void ensureId() {
        if (variantId == null) {
            variantId = UUID.randomUUID().toString();
        }
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variantId", referencedColumnName = "variant_id", insertable = false, updatable = false)
    private WarehouseStock stock;
}