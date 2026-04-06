package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<CategoryDTO> getCategoriesByType(EntryType type) {
        return categoryRepository.findByEntryType(type)
                .stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }
}
