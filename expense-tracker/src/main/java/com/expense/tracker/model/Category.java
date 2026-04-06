package com.expense.tracker.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;


    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) DEFAULT 'EXPENSE'")
    private EntryType entryType = EntryType.EXPENSE;


    @OneToMany(
            mappedBy = "category",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true
    )
    private List<Expense> expenses = new ArrayList<>();


    public Category() {}

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, EntryType entryType) {
        this.name      = name;
        this.entryType = entryType;
    }

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getName()                     { return name; }
    public void setName(String name)            { this.name = name; }

    public EntryType getEntryType()                     { return entryType; }
    public void setEntryType(EntryType entryType)       { this.entryType = entryType; }

    public List<Expense> getExpenses()                  { return expenses; }
    public void setExpenses(List<Expense> expenses)     { this.expenses = expenses; }

    public void addExpense(Expense expense) {
        this.expenses.add(expense);
        expense.setCategory(this);
    }

    public void removeExpense(Expense expense) {
        this.expenses.remove(expense);
        expense.setCategory(null);
    }
}