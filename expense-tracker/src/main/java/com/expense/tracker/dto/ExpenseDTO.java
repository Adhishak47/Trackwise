package com.expense.tracker.dto;

import com.expense.tracker.model.EntryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ExpenseDTO {
    private Long id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be greate than 0")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 100, message = "Description must be between 3 and 100 characters")
    private String description;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Type is required (INCOME or EXPENSE)")
    private EntryType type;

    @Size(max = 255, message = "Notes must be under 255 characters")
    private String notes;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String categoryName;

    public ExpenseDTO(){}

    public ExpenseDTO(Long id, BigDecimal amount, LocalDate date, String description, EntryType type, String notes, Long categoryId, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.type = type;
        this.notes = notes;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EntryType getType() { return type; }
    public void setType(EntryType type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
