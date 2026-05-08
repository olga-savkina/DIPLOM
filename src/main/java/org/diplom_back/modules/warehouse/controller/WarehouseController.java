package org.diplom_back.modules.warehouse.controller;

import org.diplom_back.modules.warehouse.dto.ProductWarehouseDTO;
import org.diplom_back.modules.warehouse.dto.ReplenishRequestDTO;
import org.diplom_back.modules.warehouse.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/warehouse")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @PutMapping("/replenish/{variantId}")
    public ResponseEntity<?> replenish(@PathVariable String variantId, @RequestBody ReplenishRequestDTO request) {
        try {
            // Логируем для проверки в консоли IDEA, что данные дошли
            System.out.println("Принято пополнение: " + request.getAmount() + ", дата: " + request.getExpiryDate());

            // Вызываем сервис напрямую, объект 'date' уже готов в 'request'
            warehouseService.replenishStock(variantId, request.getAmount(), request.getExpiryDate());

            return ResponseEntity.ok().body("Запас успешно пополнен");
        } catch (Exception e) {
            e.printStackTrace(); // Это покажет полную ошибку в консоли IDEA
            return ResponseEntity.badRequest().body("Ошибка на сервере: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<ProductWarehouseDTO>> getProductsWithStock() {
        return ResponseEntity.ok(warehouseService.getAllInventory());
    }
}