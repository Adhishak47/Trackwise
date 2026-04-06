package com.expense.tracker.dto;

import java.math.BigDecimal;

public class MonthlyTrendDTO {
    private int year;
    private int month;
    private String label;          // "Jan 2025", "Feb 2025",...

    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;        // income − expense for this month

    public MonthlyTrendDTO() {}

    public MonthlyTrendDTO(int year, int month, String label,
                           BigDecimal income, BigDecimal expense) {
        this.year    = year;
        this.month   = month;
        this.label   = label;
        this.income  = income;
        this.expense = expense;
        this.net     = income.subtract(expense);
    }

    public int getYear()                    { return year; }
    public void setYear(int year)           { this.year = year; }

    public int getMonth()                   { return month; }
    public void setMonth(int month)         { this.month = month; }

    public String getLabel()                { return label; }
    public void setLabel(String label)      { this.label = label; }

    public BigDecimal getIncome()                   { return income; }
    public void setIncome(BigDecimal income)         { this.income = income; }

    public BigDecimal getExpense()                  { return expense; }
    public void setExpense(BigDecimal expense)       { this.expense = expense; }

    public BigDecimal getNet()                      { return net; }
    public void setNet(BigDecimal net)              { this.net = net; }
}
