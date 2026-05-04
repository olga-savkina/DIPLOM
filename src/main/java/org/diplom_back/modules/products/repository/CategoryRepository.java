package org.diplom_back.modules.products.repository;

import org.diplom_back.modules.products.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByTargetType(String targetType);
}
