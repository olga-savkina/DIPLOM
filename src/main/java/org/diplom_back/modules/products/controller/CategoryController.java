package org.diplom_back.modules.products.controller;
import org.diplom_back.modules.products.entity.*;
import org.diplom_back.modules.products.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @PostMapping
    public Category create(@RequestBody Category category) {
        category.setCategoryId(UUID.randomUUID().toString());
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable String id, @RequestBody Category details) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(details.getName());
        category.setDescription(details.getDescription());
        category.setParentId(details.getParentId());
        return categoryRepository.save(category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        categoryRepository.deleteById(id);
    }
}
