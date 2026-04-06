package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.dto.PagedResponseDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.EntryType;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final int MAX_PAGE_SIZE = 100;

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

        User user = resolveUser(username);
        Category category = resolveCategory(dto.getCategoryId());

        Expense expense = new Expense();
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        expense.setType(dto.getType());
        expense.setNotes(dto.getNotes());
        expense.setUser(user);
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);
        logger.info("Record saved: id={}, type={}, user={}", saved.getId(), saved.getType(), username);
        return mapToDTO(saved);

    }

    public List<ExpenseDTO> getAllExpenses(String username) {
        logger.debug("Fetching all records for user: {}", username);
        return expenseRepository.findByUserUsername(username)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ExpenseDTO getExpenseById(Long id, String username) {
        logger.debug("Fetching expense {} for user {}", id, username);
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getUsername().equals(username))
                .orElseThrow(() -> {
                    logger.warn("Record {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
                });
        return mapToDTO(expense);
    }

    public List<ExpenseDTO> getExpensesByCategoryId(Long categoryId, String username) {
        logger.debug("Fetching records by category {} for user {}", categoryId, username);
        return expenseRepository.findByCategoryId(categoryId)
                .stream()
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

    public List<ExpenseDTO> getExpensesByType(EntryType type, String username) {
        logger.debug("Fetching {} records for user {}", type, username);
        return expenseRepository.findByUserUsernameAndType(username, type)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ExpenseDTO> getExpensesByTypeAndDateRange(EntryType type,
                                                          LocalDate start,
                                                          LocalDate end,
                                                          String username) {
        logger.debug("Fetching {} records {} to {} for user {}", type, start, end, username);
        return expenseRepository.findByUsernameAndTypeAndDateRange(username, type, start, end)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    public PagedResponseDTO<ExpenseDTO> getPagedExpenses(String username,
                                                         int page,
                                                         int size,
                                                         String sortBy,
                                                         String direction) {


        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        // Only allow sorting on safe field names — never interpolate raw strings
        String safeSort = switch (sortBy.toLowerCase()) {
            case "amount"      -> "amount";
            case "description" -> "description";
            default            -> "date";          // default and fallback
        };

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(safeSort).ascending()
                : Sort.by(safeSort).descending();

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Page<Expense> resultPage = expenseRepository.findByUserUsername(username, pageable);

        List<ExpenseDTO> content = resultPage.getContent()
                .stream().map(this::mapToDTO).collect(Collectors.toList());

        logger.debug("Paged fetch for [{}]: page={}, size={}, total={}",
                username, safePage, safeSize, resultPage.getTotalElements());

        return new PagedResponseDTO<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    // Key-words search
    public PagedResponseDTO<ExpenseDTO> searchExpenses(String username,
                                                       String keyword,
                                                       int page,
                                                       int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by("date").descending());

        if (keyword == null || keyword.isBlank()) {
            return getPagedExpenses(username, safePage, safeSize, "date", "desc");
        }

        Page<Expense> resultPage = expenseRepository
                .searchByKeyword(username, keyword.trim(), pageable);

        List<ExpenseDTO> content = resultPage.getContent()
                .stream().map(this::mapToDTO).collect(Collectors.toList());

        logger.debug("Search [{}] for keyword='{}': {} results",
                username, keyword, resultPage.getTotalElements());

        return new PagedResponseDTO<>(
                content,
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages()
        );
    }

    public BigDecimal getTotalByType(EntryType type, String username) {
        return expenseRepository.sumByUsernameAndType(username, type);
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
                    logger.warn("Record {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
                });

        Category category = resolveCategory(dto.getCategoryId());

        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        expense.setType(dto.getType());
        expense.setNotes(dto.getNotes());
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);
        logger.info("Record {} updated for user {}", id, username);
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteExpense(Long id, String username) {
        logger.debug("Deleting record {} for user {}", id, username);
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getUsername().equals(username))
                .orElseThrow(() -> {
                    logger.warn("Record {} not found for user {}", id, username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
                });
        expenseRepository.delete(expense);
        logger.info("Record {} deleted for user {}", id, username);
    }

    private ExpenseDTO mapToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setAmount(expense.getAmount());
        dto.setDescription(expense.getDescription());
        dto.setDate(expense.getDate());
        dto.setType(expense.getType());
        dto.setNotes(expense.getNotes());
        dto.setCategoryId(expense.getCategory() != null ? expense.getCategory().getId() : null);
        dto.setCategoryName(expense.getCategory() != null ? expense.getCategory().getName() : null);
        return dto;
    }

    private User resolveUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User {} not found", username);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });
    }

    private Category resolveCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    logger.error("Category {} not found", categoryId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Category not found");
                });
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
