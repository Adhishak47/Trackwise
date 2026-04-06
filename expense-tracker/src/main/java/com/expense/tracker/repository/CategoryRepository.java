package com.expense.tracker.repository;

import com.expense.tracker.model.Category;
import com.expense.tracker.model.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    List<Category> findByEntryType(EntryType entryType);
}
