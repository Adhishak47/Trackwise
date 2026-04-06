package com.expense.tracker.repository;

import com.expense.tracker.model.EntryType;
import com.expense.tracker.model.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Returns all records of a given type (INCOME or EXPENSE) for a user.
     */
    List<Expense> findByUserUsernameAndType(String username, EntryType type);

    /**
     * Returns records of a given type within a date range for a user.
     */
    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.username = :username
              AND e.type = :type
              AND e.date BETWEEN :start AND :end
            """)
    List<Expense> findByUsernameAndTypeAndDateRange(@Param("username") String username,
                                                    @Param("type") EntryType type,
                                                    @Param("start") LocalDate start,
                                                    @Param("end") LocalDate end);

    /**
     * Returns total amount for a given type for a user — used by the dashboard summary.
     */
    @Query("""
            SELECT COALESCE(SUM(e.amount), 0)
            FROM Expense e
            WHERE e.user.username = :username
              AND e.type = :type
            """)
    BigDecimal sumByUsernameAndType(@Param("username") String username,
                                    @Param("type") EntryType type);

    /**
     * Monthly totals grouped by year + month — used for trend data.
     */
    @Query("""
            SELECT FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date), SUM(e.amount)
            FROM Expense e
            WHERE e.user.username = :username
              AND e.type = :type
              AND e.date BETWEEN :start AND :end
            GROUP BY FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date)
            ORDER BY FUNCTION('YEAR', e.date), FUNCTION('MONTH', e.date)
            """)
    List<Object[]> monthlyTotals(@Param("username") String username,
                                 @Param("type") EntryType type,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);

    Page<Expense> findByUserUsername(String username, Pageable pageable);

    @Query("""
            SELECT e FROM Expense e
            WHERE e.user.username = :username
              AND (LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR  LOWER(e.notes)       LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Expense> searchByKeyword(@Param("username") String username,
                                  @Param("keyword") String keyword,
                                  Pageable pageable);
}
