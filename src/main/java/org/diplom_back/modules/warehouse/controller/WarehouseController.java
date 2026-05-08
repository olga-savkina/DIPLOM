package org.diplom_back.modules.warehouse.controller;

import org.diplom_back.modules.warehouse.dto.ProductWarehouseDTO;
import org.diplom_back.modules.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @PutMapping("/replenish/{variantId}")
    public ResponseEntity<?> replenish(@PathVariable String variantId, @RequestBody Integer amount) {
        try {
            warehouseService.replenishStock(variantId, amount);
            return ResponseEntity.ok().body("Запас успешно пополнен");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при пополнении склада: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<List<ProductWarehouseDTO>> getProductsWithStock() {
        return ResponseEntity.ok(warehouseService.getAllInventory());
    }
}