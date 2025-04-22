package com.tmd.platform.service;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.Payment;
import com.tmd.platform.domain.User;
import com.tmd.platform.dto.LoanDetailResponse;
import com.tmd.platform.dto.LoanRequest;
import com.tmd.platform.repository.LoanRepository;
import com.tmd.platform.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Loan loan;
    private LoanRequest loanRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .role("USER")
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

        payment = Payment.builder()
                .id(1L)
                .loan(loan)
                .amount(new BigDecimal("100.00"))
                .dueDate(LocalDateTime.now().plusMonths(1))
                .status(Payment.PaymentStatus.PENDING)
                .build();

        // Mock security context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createLoan_ShouldCreateNewLoan() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.createLoan(loanRequest);

        assertNotNull(result);
        assertEquals(loan.getId(), result.getId());
        assertEquals(loan.getAmount(), result.getAmount());
        assertEquals(loan.getInterestRate(), result.getInterestRate());
        assertEquals(loan.getTermMonths(), result.getTermMonths());
        assertEquals(Loan.LoanStatus.PENDING, result.getStatus());
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void getUserLoans_ShouldReturnUserLoans() {
        Page<Loan> loanPage = new PageImpl<>(Collections.singletonList(loan));
        when(userService.getCurrentUser()).thenReturn(user);
        when(loanRepository.findByUser(any(User.class), any(Pageable.class))).thenReturn(loanPage);

        Page<Loan> result = loanService.getUserLoans(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(loan.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getLoanDetails_ShouldReturnLoanDetails() {
        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(userService.getCurrentUser()).thenReturn(user);
        when(paymentRepository.findByLoan(any(Loan.class))).thenReturn(Collections.singletonList(payment));

        LoanDetailResponse result = loanService.getLoanDetails(1L);

        assertNotNull(result);
        assertEquals(loan.getId(), result.getId());
        assertEquals(loan.getAmount(), result.getAmount());
        assertEquals(loan.getInterestRate(), result.getInterestRate());
        assertEquals(loan.getTermMonths(), result.getTermMonths());
        assertEquals(loan.getStatus(), result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getTotalPaid());
        assertEquals(loan.getAmount(), result.getRemainingAmount());
        assertEquals(1, result.getPayments().size());
    }

    @Test
    void approveLoan_ShouldApproveLoanAndGeneratePayments() {
        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(paymentRepository.saveAll(any(List.class))).thenReturn(Collections.singletonList(payment));

        Loan result = loanService.approveLoan(1L);

        assertNotNull(result);
        assertEquals(Loan.LoanStatus.APPROVED, result.getStatus());
        assertNotNull(result.getApprovedAt());
        verify(paymentRepository).saveAll(any(List.class));
    }

    @Test
    void rejectLoan_ShouldRejectLoan() {
        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.rejectLoan(1L);

        assertNotNull(result);
        assertEquals(Loan.LoanStatus.REJECTED, result.getStatus());
    }

    @Test
    void cancelLoan_ShouldCancelLoan() {
        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(userService.getCurrentUser()).thenReturn(user);
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.cancelLoan(1L);

        assertNotNull(result);
        assertEquals(Loan.LoanStatus.REJECTED, result.getStatus());
    }

    @Test
    void cancelLoan_ShouldThrowExceptionWhenNotOwner() {
        User otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .build();

        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(userService.getCurrentUser()).thenReturn(otherUser);

        assertThrows(RuntimeException.class, () -> loanService.cancelLoan(1L));
    }

    @Test
    void cancelLoan_ShouldThrowExceptionWhenNotPending() {
        loan.setStatus(Loan.LoanStatus.APPROVED);
        when(loanRepository.findById(any(Long.class))).thenReturn(Optional.of(loan));
        when(userService.getCurrentUser()).thenReturn(user);

        assertThrows(RuntimeException.class, () -> loanService.cancelLoan(1L));
    }
} 