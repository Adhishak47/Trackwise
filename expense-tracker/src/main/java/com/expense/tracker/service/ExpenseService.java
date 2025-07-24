package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private static final Logger logger = LoggerFactory.getLogger(ExpenseService.class);

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.modelMapper = new ModelMapper();
    }

    public ExpenseDTO saveExpense(ExpenseDTO dto, String username) {
        logger.debug("Saving expense for user: {}", username);
        validateExpense(dto);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User {} not found", username);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    logger.error("Category {} not found", dto.getCategoryId());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found");
                });

        Expense expense = modelMapper.map(dto, Expense.class);
        expense.setUser(user);
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);
        logger.info("Expense saved successfully: {}", saved.getId());

        ExpenseDTO result = modelMapper.map(saved, ExpenseDTO.class);
        result.setCategoryName(category.getName());
        return result;
    }

    public List<ExpenseDTO> getAllExpenses(String username) {
        logger.debug("Fetching all expenses for user: {}", username);
        return expenseRepository.findByUserUsername(username).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id, String username) {
        logger.debug("Fetching expense {} for user {}", id, username);
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getUsername().equals(username))
                .orElseThrow(() -> {
                    logger.warn("Expense {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
                });
        return mapToDTO(expense);
    }

    public List<ExpenseDTO> getExpensesByCategoryId(Long categoryId, String username) {
        logger.debug("Fetching expenses by category {} for user {}", categoryId, username);
        return expenseRepository.findByCategoryId(categoryId).stream()
                .filter(e -> e.getUser().getUsername().equals(username))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByDateRange(LocalDate start, LocalDate end, String username) {
        logger.debug("Fetching expenses from {} to {} for user {}", start, end, username);
        return expenseRepository.findByDateRange(start, end).stream()
                .filter(e -> e.getUser().getUsername().equals(username))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByCategoryAndDate(String categoryName, LocalDate start, LocalDate end, String username) {
        logger.debug("Fetching expenses by category name '{}' from {} to {} for user {}", categoryName, start, end, username);
        return expenseRepository.findByDateRange(start, end).stream()
                .filter(e -> e.getUser().getUsername().equals(username))
                .filter(e -> e.getCategory().getName().equalsIgnoreCase(categoryName))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalExpensesByCategoryId(Long categoryId, String username) {
        logger.debug("Calculating total for category {} and user {}", categoryId, username);
        return getExpensesByCategoryId(categoryId, username).stream()
                .map(ExpenseDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ExpenseDTO updateExpense(Long id, ExpenseDTO dto, String username) {
        logger.debug("Updating expense {} for user {}", id, username);
        validateExpense(dto);

        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getUsername().equals(username))
                .orElseThrow(() -> {
                    logger.warn("Expense {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
                });

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> {
                    logger.warn("Invalid category id {} for update", dto.getCategoryId());
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category");
                });

        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);
        logger.info("Expense {} updated for user {}", id, username);
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        logger.debug("Deleting expense {} for user {}", id, username);
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getUsername().equals(username))
                .orElseThrow(() -> {
                    logger.warn("Expense {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found");
                });
        expenseRepository.delete(expense);
        logger.info("Expense {} deleted for user {}", id, username);
    }

    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = modelMapper.map(expense, ExpenseDTO.class);
        dto.setCategoryName(expense.getCategory().getName());
        return dto;
    }

    private void validateExpense(ExpenseDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Invalid amount: {}", dto.getAmount());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            logger.warn("Missing description");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
        }
        if (dto.getDate() == null || dto.getDate().isAfter(LocalDate.now())) {
            logger.warn("Invalid date: {}", dto.getDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date");
        }
        if (dto.getCategoryId() == null || !categoryRepository.existsById(dto.getCategoryId())) {
            logger.warn("Invalid category ID: {}", dto.getCategoryId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category ID");
        }
    }
}
