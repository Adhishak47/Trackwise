package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.dto.PagedResponseDTO;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Financial Records", description = "REST endpoints for managing financial records")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new financial record [ADMIN]")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO,
                                                    Authentication auth) {
        logger.info("Creating {} record for user: {}", expenseDTO.getType(), auth.getName());
        ExpenseDTO created = expenseService.saveExpense(expenseDTO, auth.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }


    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get ALL records unpaginated — use /api/expenses for paged [VIEWER+]")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses(Authentication auth) {
        return ResponseEntity.ok(expenseService.getAllExpenses(auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "List records with pagination and sorting [VIEWER, ANALYST, ADMIN]")
    public ResponseEntity<PagedResponseDTO<ExpenseDTO>> getPagedExpenses(
            @RequestParam(defaultValue = "0")    int    page,
            @RequestParam(defaultValue = "10")   int    size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Authentication auth) {

        logger.info("Paged records for [{}]: page={}, size={}, sort={} {}",
                auth.getName(), page, size, sortBy, direction);

        return ResponseEntity.ok(
                expenseService.getPagedExpenses(
                        auth.getName(), page, size, sortBy, direction));
    }

    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Search records by keyword in description or notes [VIEWER+]")
    public ResponseEntity<PagedResponseDTO<ExpenseDTO>> searchExpenses(
            @RequestParam                      String keyword,
            @RequestParam(defaultValue = "0")  int    page,
            @RequestParam(defaultValue = "10") int    size,
            Authentication auth) {

        logger.info("Search [{}] for keyword='{}'", auth.getName(), keyword);

        return ResponseEntity.ok(
                expenseService.searchExpenses(auth.getName(), keyword, page, size));
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get records by category [VIEWER, ANALYST, ADMIN]")
    public ResponseEntity<List<ExpenseDTO>> getByCategory(@PathVariable Long categoryId,
                                                          Authentication auth) {
        return ResponseEntity.ok(
                expenseService.getExpensesByCategoryId(categoryId, auth.getName()));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get records within a date range [VIEWER, ANALYST, ADMIN]")
    public ResponseEntity<List<ExpenseDTO>> getByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication auth) {
        return ResponseEntity.ok(
                expenseService.getExpensesByDateRange(startDate, endDate, auth.getName()));
    }

    /**
     * Filter records by type — GET /api/expenses/by-type/INCOME
     *                          GET /api/expenses/by-type/EXPENSE
     */
    @GetMapping("/by-type/{type}")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get records by type — INCOME or EXPENSE [VIEWER, ANALYST, ADMIN]")
    public ResponseEntity<List<ExpenseDTO>> getByType(@PathVariable EntryType type,
                                                      Authentication auth) {
        logger.info("Fetching {} records for user: {}", type, auth.getName());
        return ResponseEntity.ok(expenseService.getExpensesByType(type, auth.getName()));
    }

    /**
     * Filter by type + date range —
     * GET /api/expenses/by-type/INCOME/date-range?startDate=...&endDate=...
     */
    @GetMapping("/by-type/{type}/date-range")
    @PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
    @Operation(summary = "Get records by type within a date range [VIEWER, ANALYST, ADMIN]")
    public ResponseEntity<List<ExpenseDTO>> getByTypeAndDateRange(
            @PathVariable EntryType type,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication auth) {
        logger.info("Fetching {} records {} to {} for user: {}",
                type, startDate, endDate, auth.getName());
        return ResponseEntity.ok(
                expenseService.getExpensesByTypeAndDateRange(
                        type, startDate, endDate, auth.getName()));
    }

    @GetMapping("/total/category/{categoryId}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @Operation(summary = "Get total amount for a category [ANALYST, ADMIN]")    public ResponseEntity<BigDecimal> getTotalByCategory(@PathVariable Long categoryId,
                                                         Authentication auth) {
        return ResponseEntity.ok(
                expenseService.getTotalExpensesByCategoryId(categoryId, auth.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a record [ADMIN]")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id,
                                                    @Valid @RequestBody ExpenseDTO expenseDTO,
                                                    Authentication auth) {
        logger.info("Updating record {} for user {}", id, auth.getName());
        return ResponseEntity.ok(expenseService.updateExpense(id, expenseDTO, auth.getName()));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a record [ADMIN]")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id, Authentication auth) {
        logger.info("Deleting record {} for user {}", id, auth.getName());
        expenseService.deleteExpense(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
