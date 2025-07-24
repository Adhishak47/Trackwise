package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.model.Category;
import com.expense.tracker.model.Expense;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = ExpenseServiceTest.MockedConfig.class)
public class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("100.00"));
        expense.setDescription("Lunch");
        expense.setDate(LocalDate.now());

        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        User user = new User();
        user.setUsername("john");

        // ✅ Mock behavior
        Mockito.when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        Mockito.when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Mockito.when(categoryRepository.existsById(1L)).thenReturn(true);
        Mockito.when(expenseRepository.save(Mockito.any(Expense.class))).thenReturn(expense);
    }

    @Test
    void testSaveExpense_Success() {
        ExpenseDTO dto = new ExpenseDTO(null, new BigDecimal("100.00"), LocalDate.now(), "Lunch", 1L, null);
        ExpenseDTO saved = expenseService.saveExpense(dto, "john");

        assertThat(saved).isNotNull();
        assertThat(saved.getAmount()).isEqualByComparingTo("100.00");
        assertThat(saved.getDescription()).isEqualTo("Lunch");
    }

    @TestConfiguration
    static class MockedConfig {

        @Bean
        public ExpenseRepository expenseRepository() {
            return Mockito.mock(ExpenseRepository.class);
        }

        @Bean
        public CategoryRepository categoryRepository() {
            return Mockito.mock(CategoryRepository.class);
        }

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public ExpenseService expenseService(ExpenseRepository expenseRepository,
                                             CategoryRepository categoryRepository,
                                             UserRepository userRepository) {
            return new ExpenseService(expenseRepository, categoryRepository, userRepository);
        }

        @Bean
        public ModelMapper modelMapper() {
            return new ModelMapper();
        }
    }
}
