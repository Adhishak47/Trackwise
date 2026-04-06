package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardSummaryDTO {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;  // totalIncome − totalExpenses

    private String periodStart;
    private String periodEnd;

    private Map<String, BigDecimal> categoryBreakdown;

    private List<MonthlyTrendDTO> monthlyTrends;

    public DashboardSummaryDTO() {}

    public BigDecimal getTotalIncome()                      { return totalIncome; }
    public void setTotalIncome(BigDecimal v)                { this.totalIncome = v; }

    public BigDecimal getTotalExpenses()                    { return totalExpenses; }
    public void setTotalExpenses(BigDecimal v)              { this.totalExpenses = v; }

    public BigDecimal getNetBalance()                       { return netBalance; }
    public void setNetBalance(BigDecimal v)                 { this.netBalance = v; }

    public String getPeriodStart()                          { return periodStart; }
    public void setPeriodStart(String v)                    { this.periodStart = v; }

    public String getPeriodEnd()                            { return periodEnd; }
    public void setPeriodEnd(String v)                      { this.periodEnd = v; }

    public Map<String, BigDecimal> getCategoryBreakdown()           { return categoryBreakdown; }
    public void setCategoryBreakdown(Map<String, BigDecimal> v)     { this.categoryBreakdown = v; }

    public List<MonthlyTrendDTO> getMonthlyTrends()                 { return monthlyTrends; }
    public void setMonthlyTrends(List<MonthlyTrendDTO> v)           { this.monthlyTrends = v; }
}
