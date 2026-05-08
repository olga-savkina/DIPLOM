package org.diplom_back.modules.warehouse.dto;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public class ReplenishRequestDTO {
    private Integer amount;

    // Эта аннотация скажет Spring, как превратить строку "2026-08-06" в дату
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    // Геттеры и сеттеры
    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}