package org.diplom_back.modules.products.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @Column(name = "variant_id", length = 36) // Обязательно имя как в базе для связи @OneToOne
    private String variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonBackReference
    private Product product;

    @Column(name = "sku", unique = true)
    private String sku;

    private String size;
    private String color;

    @Column(name = "price_override")
    private BigDecimal priceOverride;

    @Column(name = "age_min")
    private Integer ageMin;

    @Column(name = "age_max")
    private Integer ageMax;
    @Transient
    // ВАЖНО: Эти поля пока остаются, если база еще не обновлена,
    // но в будущем их нужно удалить, так как данные теперь в WarehouseStock
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Transient
    @Column(name = "production_date")
    private LocalDate productionDate;
    @Transient
    // Старое поле для совместимости (пока не удалено в БД)
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    // СВЯЗЬ СО СКЛАДОМ
    // mappedBy = "variant" указывает на поле 'variant' в классе WarehouseStock
    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private WarehouseStock stock;

    @PrePersist
    public void ensureId() {
        if (variantId == null) {
            variantId = UUID.randomUUID().toString();
        }
    }
}