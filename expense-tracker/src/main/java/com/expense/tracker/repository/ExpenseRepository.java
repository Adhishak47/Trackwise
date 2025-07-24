package com.expense.tracker.repository;

import com.expense.tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.category.id = ?1")
    List<Expense> findByCategoryId(Long categoryId);

    @Query("SELECT e FROM Expense e WHERE e.date BETWEEN ?1 AND ?2")
    List<Expense> findByDateRange(LocalDate start, LocalDate end);

    List<Expense> findByCategoryNameAndDateBetween(String categoryName, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category.id = ?1")
    Optional<BigDecimal> getTotalExpensesByCategoryId(Long categoryId);

    List<Expense> findByUserUsername(String username);

}
