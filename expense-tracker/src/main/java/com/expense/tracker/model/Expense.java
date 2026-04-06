package com.expense.tracker.model;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses",
        indexes = @Index(name = "idx_expense_date", columnList = "expense_date"))
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 100, nullable = false)
    private String description;

    @Column(name = "expense_date", columnDefinition = "DATE", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) DEFAULT 'EXPENSE'")
    private EntryType type = EntryType.EXPENSE;

    @Column(name = "notes", length = 255)
    private String notes;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JoinColumn(
            name = "category_id",
            foreignKey = @ForeignKey(name = "fk_expense_category"),
            nullable = true
    )
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false) // matches User primary key
    private User user;


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

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Expense(){}

    public Expense(BigDecimal amount, String description, LocalDate date, Category category, EntryType type) {
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.category = category;
        this.type = type;
    }
}
