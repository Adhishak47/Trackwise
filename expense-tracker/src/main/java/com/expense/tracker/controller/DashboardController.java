package com.expense.tracker.controller;

import com.expense.tracker.dto.DashboardSummaryDTO;
import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.service.DashboardService;
import com.expense.tracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Dashboard", description = "Aggregated summary endpoints. Requires ANALYST or ADMIN.")
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final ExpenseService expenseService;

    @Autowired
    public DashboardController(DashboardService dashboardService,
                               ExpenseService expenseService) {
        this.dashboardService = dashboardService;
        this.expenseService   = expenseService;
    }

    /**
     * GET /api/dashboard/summary
     * GET /api/dashboard/summary?startDate=2025-01-01&endDate=2025-03-31
     */
    @GetMapping("/summary")
    @Operation(summary = "Full dashboard summary with KPIs, breakdown, and trends [ANALYST, ADMIN]")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            Authentication auth) {

        logger.info("Dashboard summary requested by [{}], range: {} → {}",
                auth.getName(), startDate, endDate);

        DashboardSummaryDTO summary =
                dashboardService.buildSummary(auth.getName(), startDate, endDate);

        return ResponseEntity.ok(summary);
    }

    /**
     * GET /api/dashboard/recent?limit=10
     */
    @GetMapping("/recent")
    @Operation(summary = "N most recent records, newest first [ANALYST, ADMIN]")
    public ResponseEntity<List<ExpenseDTO>> getRecentActivity(
            @RequestParam(defaultValue = "10") int limit,
            Authentication auth) {

        int safeLimit = Math.min(limit, 50);

        logger.info("Recent activity requested by [{}], limit={}",
                auth.getName(), safeLimit);

        List<ExpenseDTO> recent = expenseService.getAllExpenses(auth.getName())
                .stream()
                .sorted(Comparator.comparing(ExpenseDTO::getDate).reversed())
                .limit(safeLimit)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recent);
    }
}
