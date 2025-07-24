package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Expense Controller", description = "REST endpoints for managing expenses")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/expenses")
public class ExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService){
        this.expenseService = expenseService;
    }

    @GetMapping("/profile")
    public String userProfile() {
        return "profile"; // profile.html
    }

    @GetMapping("/settings")
    public String userSettings() {
        return "settings"; // settings.html
    }

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO, Authentication auth) {
        logger.info("Creating expense for user: {}", auth.getName());
        ExpenseDTO created = expenseService.saveExpense(expenseDTO, auth.getName());
        logger.debug("Expense created: {}", created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all expenses for current user")
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses(Authentication auth) {
        logger.info("Fetching all expenses for user: {}", auth.getName());
        List<ExpenseDTO> expenses = expenseService.getAllExpenses(auth.getName());
        logger.debug("Expenses retrieved: {}", expenses.size());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get expenses by category")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByCategoryId(@PathVariable Long categoryId, Authentication auth) {
        logger.info("Fetching expenses by category {} for user: {}", categoryId, auth.getName());
        return ResponseEntity.ok(expenseService.getExpensesByCategoryId(categoryId, auth.getName()));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get expenses in date range")
    public ResponseEntity<List<ExpenseDTO>> getExpensesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            Authentication auth) {
        logger.info("Fetching expenses from {} to {} for user: {}", startDate, endDate, auth.getName());
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(startDate, endDate, auth.getName()));
    }

    @GetMapping("/total/category/{categoryId}")
    @Operation(summary = "Get total by category")
    public ResponseEntity<BigDecimal> getTotalExpensesByCategory(@PathVariable Long categoryId, Authentication auth) {
        logger.info("Calculating total expenses for category {} and user {}", categoryId, auth.getName());
        return ResponseEntity.ok(expenseService.getTotalExpensesByCategoryId(categoryId, auth.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense")
    public ResponseEntity<ExpenseDTO> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseDTO expenseDTO,
            Authentication auth) {
        logger.info("Updating expense with id {} for user {}", id, auth.getName());
        ExpenseDTO updated = expenseService.updateExpense(id, expenseDTO, auth.getName());
        logger.debug("Expense updated: {}", updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Authentication auth) {
        logger.info("Deleting expense with id {} for user {}", id, auth.getName());
        expenseService.deleteExpense(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleExpenseException(ResponseStatusException ex) {
        logger.error("Error: {}", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }
}
