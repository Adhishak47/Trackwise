package com.expense.tracker.service;

import com.expense.tracker.dto.DashboardSummaryDTO;
import com.expense.tracker.dto.MonthlyTrendDTO;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    @Autowired
    public DashboardService(ExpenseRepository expenseRepository,
                            ExpenseService expenseService) {
        this.expenseRepository = expenseRepository;
        this.expenseService    = expenseService;
    }


    public DashboardSummaryDTO buildSummary(String username,
                                            LocalDate startDate,
                                            LocalDate endDate) {
        //  Default range: current month
        LocalDate effectiveEnd   = (endDate   != null) ? endDate   : LocalDate.now();
        LocalDate effectiveStart = (startDate != null) ? startDate
                : effectiveEnd.withDayOfMonth(1);

        logger.debug("Building dashboard for [{}] from {} to {}",
                username, effectiveStart, effectiveEnd);


        BigDecimal totalIncome   = expenseRepository
                .sumByUsernameAndType(username, EntryType.INCOME);
        BigDecimal totalExpenses = expenseRepository
                .sumByUsernameAndType(username, EntryType.EXPENSE);


        if (totalIncome   == null) totalIncome   = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        // Category breakdown (expenses only, within date range)
        Map<String, BigDecimal> categoryBreakdown = expenseService
                .getExpensesByTypeAndDateRange(
                        EntryType.EXPENSE, effectiveStart, effectiveEnd, username)
                .stream()
                .filter(dto -> dto.getCategoryName() != null)
                .collect(Collectors.groupingBy(
                        dto -> dto.getCategoryName(),
                        Collectors.reducing(BigDecimal.ZERO,
                                dto -> dto.getAmount(),
                                BigDecimal::add)
                ));

        // Sort descending by amount — largest spending category first
        Map<String, BigDecimal> sortedBreakdown = categoryBreakdown.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,         // merge function — won't trigger, keys are unique
                        LinkedHashMap::new   // map factory — preserves insertion order
                ));

        //Monthly trends
        List<MonthlyTrendDTO> monthlyTrends = buildMonthlyTrends(
                username, effectiveStart, effectiveEnd);

        // Assemble
        DashboardSummaryDTO summary = new DashboardSummaryDTO();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpenses(totalExpenses);
        summary.setNetBalance(netBalance);
        summary.setPeriodStart(effectiveStart.toString());
        summary.setPeriodEnd(effectiveEnd.toString());
        summary.setCategoryBreakdown(sortedBreakdown);
        summary.setMonthlyTrends(monthlyTrends);

        logger.info("Dashboard built for [{}]: income={}, expenses={}, net={}",
                username, totalIncome, totalExpenses, netBalance);

        return summary;
    }


    /**
     * Builds one MonthlyTrendDTO per calendar month between startDate and endDate.
     */
    private List<MonthlyTrendDTO> buildMonthlyTrends(String username,
                                                     LocalDate start,
                                                     LocalDate end) {
        List<Object[]> incomeRows  = expenseRepository
                .monthlyTotals(username, EntryType.INCOME,  start, end);
        List<Object[]> expenseRows = expenseRepository
                .monthlyTotals(username, EntryType.EXPENSE, start, end);

        Map<String, BigDecimal> incomeMap  = indexByYearMonth(incomeRows);
        Map<String, BigDecimal> expenseMap = indexByYearMonth(expenseRows);

        List<MonthlyTrendDTO> trends = new ArrayList<>();
        LocalDate cursor = start.withDayOfMonth(1);
        LocalDate endMonth = end.withDayOfMonth(1);

        while (!cursor.isAfter(endMonth)) {
            String key     = cursor.getYear() + "-" + cursor.getMonthValue();
            String label   = buildLabel(cursor);

            BigDecimal inc = incomeMap .getOrDefault(key, BigDecimal.ZERO);
            BigDecimal exp = expenseMap.getOrDefault(key, BigDecimal.ZERO);

            trends.add(new MonthlyTrendDTO(
                    cursor.getYear(), cursor.getMonthValue(), label, inc, exp));

            cursor = cursor.plusMonths(1);
        }

        return trends;
    }


    private Map<String, BigDecimal> indexByYearMonth(List<Object[]> rows) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            int year   = ((Number) row[0]).intValue();
            int month  = ((Number) row[1]).intValue();
            BigDecimal amount = (row[2] instanceof BigDecimal)
                    ? (BigDecimal) row[2]
                    : BigDecimal.valueOf(((Number) row[2]).doubleValue());
            map.put(year + "-" + month, amount);
        }
        return map;
    }

    private String buildLabel(LocalDate cursor) {
        String monthName = Month.of(cursor.getMonthValue())
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        return monthName + " " + cursor.getYear();
    }
}