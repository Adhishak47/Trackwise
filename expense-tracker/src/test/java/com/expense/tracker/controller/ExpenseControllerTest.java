package com.expense.tracker.controller;

import com.expense.tracker.dto.ExpenseDTO;
import com.expense.tracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@WithMockUser(username = "john")  // Simulate logged-in user
@Import(ExpenseControllerTest.MockedServiceConfig.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static ExpenseService expenseServiceMock;

    private ExpenseDTO expenseDTO;

    @BeforeEach
    void setUp() {
        expenseDTO = new ExpenseDTO();
        expenseDTO.setId(1L);
        expenseDTO.setAmount(new BigDecimal("100.00"));
        expenseDTO.setDescription("Test Expense");
        expenseDTO.setDate(LocalDate.now());
        expenseDTO.setCategoryId(1L);
        expenseDTO.setCategoryName("Food");
    }

    @Test
    void shouldReturnListOfExpenses() throws Exception {
        Mockito.when(expenseServiceMock.getAllExpenses("john")).thenReturn(List.of(expenseDTO));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(expenseDTO))));
    }

    @Test
    void shouldCreateExpense() throws Exception {
        Mockito.when(expenseServiceMock.saveExpense(any(), eq("john"))).thenReturn(expenseDTO);

        mockMvc.perform(post("/api/expenses")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(expenseDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(expenseDTO)));
    }

    @TestConfiguration
    static class MockedServiceConfig {

        @Bean
        public ExpenseService expenseService() {
            expenseServiceMock = Mockito.mock(ExpenseService.class);
            return expenseServiceMock;
        }
    }
}
