package com.expense.tracker.controller;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Categories", description = "Fetch categories filtered by entry type")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping
    @Operation(summary = "Get categories filtered by entry type (INCOME or EXPENSE)")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByType(
            @RequestParam EntryType type) {
        return ResponseEntity.ok(categoryService.getCategoriesByType(type));
    }
}
