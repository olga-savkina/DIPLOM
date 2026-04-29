package org.diplom_back.modules.products.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @Column(name = "category_id", length = 36)
    private String categoryId;

    @Column(name = "parent_id", length = 36)
    private String parentId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;
    // Связь многие-ко-многим с товарами
    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private List<Product> products;
}