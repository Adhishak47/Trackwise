package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.service.CategoryService;
import com.expense.tracker.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    public HomeController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    // Home page - expense list
    @GetMapping("/")
    public String home(Model model, Principal principal) {
        String username = principal.getName();
        List<ExpenseDTO> expenses = expenseService.getAllExpenses(username);

        // Calculate total of current month
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        BigDecimal expensesTotal = expenses.stream()
                .filter(e -> !e.getDate().isBefore(start) && !e.getDate().isAfter(now))
                .map(ExpenseDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("expenses", expenses);
        model.addAttribute("expensesTotal", expensesTotal);

        return "index";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model, Principal principal) {
        // You can fetch logged-in user info and add to model if needed
        return "profile"; // maps to profile.html
    }

    @GetMapping("/settings")
    public String showSettingsPage() {
        return "settings"; // maps to settings.html
    }

    @PostMapping("/settings")
    public String updateSettings(
            @RequestParam String email,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            Principal principal
    ) {
        // TODO: Add validation, password change logic etc.

        // Redirect to home or profile after success
        return "redirect:/";
    }


    // Show form to add new expense
    @GetMapping("/add-expense")
    public String showAddExpenseForm(Model model) {
        model.addAttribute("expense", new ExpenseDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "add-expense";
    }

    // Save new or updated expense
    @PostMapping("/expenses")
    public String saveOrUpdateExpense(
            @Valid @ModelAttribute("expense") ExpenseDTO expenseDTO,
            BindingResult bindingResult,
            Model model,
            Authentication auth) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return (expenseDTO.getId() != null) ? "edit-expense" : "add-expense";
        }

        try {
            if (expenseDTO.getId() != null) {
                expenseService.updateExpense(expenseDTO.getId(), expenseDTO, auth.getName());
            } else {
                expenseService.saveExpense(expenseDTO, auth.getName());
            }
            return "redirect:/";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("expense", expenseDTO);
            return (expenseDTO.getId() != null) ? "edit-expense" : "add-expense";
        }
    }

    // Show edit form
    @GetMapping("/expenses/{id}/edit")
    public String showEditExpenseForm(@PathVariable Long id, Model model, Authentication auth) {
        ExpenseDTO expense = expenseService.getExpenseById(id, auth.getName());
        model.addAttribute("expense", expense);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "edit-expense";
    }

    // Delete expense
    @PostMapping("/expenses/{id}/delete")
    public String deleteExpense(@PathVariable Long id, Authentication auth) {
        expenseService.deleteExpense(id, auth.getName());
        return "redirect:/";
    }

    // Reports page (GET UI)
    @GetMapping("/reports")
    public String showReports(Model model, Authentication auth) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.withDayOfMonth(1);

        List<ExpenseDTO> expenses = expenseService.getExpensesByDateRange(start, end, auth.getName());

        double total = expenses.stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();

        Map<String, Double> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseDTO::getCategoryName,
                        Collectors.summingDouble(e -> e.getAmount().doubleValue())
                ));

        model.addAttribute("currentDate", end);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("expensesTotal", total);
        model.addAttribute("categories", new ArrayList<>(categoryMap.keySet()));
        model.addAttribute("categoryAmounts", new ArrayList<>(categoryMap.values()));

        return "reports";
    }

    // Reports chart API
    @GetMapping("/reports/data")
    @ResponseBody
    public Map<String, Object> getReportData(@RequestParam String startDate,
                                             @RequestParam String endDate,
                                             Authentication auth) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<ExpenseDTO> expenses = expenseService.getExpensesByDateRange(start, end, auth.getName());

        double totalExpenses = expenses.stream()
                .mapToDouble(e -> e.getAmount().doubleValue())
                .sum();

        Map<String, Double> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseDTO::getCategoryName,
                        Collectors.summingDouble(e -> e.getAmount().doubleValue())
                ));

        Map<String, Object> result = new HashMap<>();
        result.put("totalExpenses", totalExpenses);
        result.put("categories", new ArrayList<>(categoryMap.keySet()));
        result.put("categoryAmounts", new ArrayList<>(categoryMap.values()));

        return result;
    }

    // Category drill-down endpoint
    @GetMapping("/reports/category-details")
    @ResponseBody
    public List<ExpenseDTO> getCategoryDetails(@RequestParam String category,
                                               @RequestParam String startDate,
                                               @RequestParam String endDate,
                                               Authentication auth) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return expenseService.getExpensesByCategoryAndDate(category, start, end, auth.getName());
    }
}
