package com.tmd.platform.controller;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.User;
import com.tmd.platform.dto.LoanDetailResponse;
import com.tmd.platform.dto.LoanRequest;
import com.tmd.platform.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    private Loan loan;
    private LoanRequest loanRequest;
    private LoanDetailResponse loanDetailResponse;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .build();

        loan = Loan.builder()
                .id(1L)
                .user(user)
                .amount(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("5.0"))
                .termMonths(12)
                .status(Loan.LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        loanRequest = LoanRequest.builder()
                .amount(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("5.0"))
                .termMonths(12)
                .build();

        loanDetailResponse = LoanDetailResponse.builder()
                .id(1L)
                .amount(new BigDecimal("1000.00"))
                .interestRate(new BigDecimal("5.0"))
                .termMonths(12)
                .status(Loan.LoanStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalPaid(BigDecimal.ZERO)
                .remainingAmount(new BigDecimal("1000.00"))
                .payments(Collections.emptyList())
                .build();
    }

    @Test
    @WithMockUser
    void createLoan_ShouldReturnCreatedLoan() throws Exception {
        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(loan);

        mockMvc.perform(post("/api/v1/loans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":1000.00,\"interestRate\":5.0,\"termMonths\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.interestRate").value(5.0))
                .andExpect(jsonPath("$.termMonths").value(12));
    }

    @Test
    @WithMockUser
    void getMyLoans_ShouldReturnUserLoans() throws Exception {
        Page<Loan> loanPage = new PageImpl<>(Collections.singletonList(loan));
        when(loanService.getUserLoans(any(Pageable.class))).thenReturn(loanPage);

        mockMvc.perform(get("/api/v1/loans/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllLoans_ShouldReturnAllLoans() throws Exception {
        Page<Loan> loanPage = new PageImpl<>(Collections.singletonList(loan));
        when(loanService.getAllLoans(any(Pageable.class))).thenReturn(loanPage);

        mockMvc.perform(get("/api/v1/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(1000.00));
    }

    @Test
    @WithMockUser
    void getLoan_ShouldReturnLoanDetails() throws Exception {
        when(loanService.getLoanDetails(any(Long.class))).thenReturn(loanDetailResponse);

        mockMvc.perform(get("/api/v1/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.totalPaid").value(0))
                .andExpect(jsonPath("$.remainingAmount").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveLoan_ShouldApproveLoan() throws Exception {
        when(loanService.approveLoan(any(Long.class))).thenReturn(loan);

        mockMvc.perform(post("/api/v1/loans/1/approve")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectLoan_ShouldRejectLoan() throws Exception {
        when(loanService.rejectLoan(any(Long.class))).thenReturn(loan);

        mockMvc.perform(post("/api/v1/loans/1/reject")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void cancelLoan_ShouldCancelLoan() throws Exception {
        when(loanService.cancelLoan(any(Long.class))).thenReturn(loan);

        mockMvc.perform(post("/api/v1/loans/1/cancel")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
} 