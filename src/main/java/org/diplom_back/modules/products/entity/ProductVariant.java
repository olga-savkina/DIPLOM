package org.diplom_back.modules.products.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference // Чтобы не было бесконечной рекурсии при сериализации в JSON
    private Product product;

    private String size;  // Например: "92", "104", "XL"
    private String color; // Например: "Бежевый"
    private Integer stockQuantity; // Остаток на складе
    private String sku; // Артикул, например: "GIRL-SUIT-BEG-104"

    @PrePersist
    public void ensureId() {
        if (variantId == null) {
            variantId = UUID.randomUUID().toString();
        }
    }
}
