package com.tmd.platform.service;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.Payment;
import com.tmd.platform.domain.User;
import com.tmd.platform.dto.LoanDetailResponse;
import com.tmd.platform.dto.LoanRequest;
import com.tmd.platform.exception.AppException;
import com.tmd.platform.repository.BankCardRepository;
import com.tmd.platform.repository.LoanRepository;
import com.tmd.platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final BankCardRepository bankCardRepository;

    @Transactional
    public Loan createLoan(LoanRequest request) {
        User user = userService.getCurrentUser();

        // Check if user has a bank card
        if (!bankCardRepository.existsByUser(user)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "BANK_CARD_REQUIRED", "Please add a bank card before applying for a loan");
        }

        Loan loan = Loan.builder()
                .user(user)
                .amount(request.getAmount())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .status(Loan.LoanStatus.PENDING)
                .build();

        loan = loanRepository.save(loan);
        return loan;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Loan approveLoan(Long loanId) {
        Loan loan = getLoanById(loanId);
        User currentUser = userService.getCurrentUser();
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalStateException("Loan is not in PENDING state");
        }
        if (currentUser.getId().equals(loan.getUser().getId())) {
            throw new RuntimeException("You can only approve your own loans");
        }

        loan.setStatus(Loan.LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        loan = loanRepository.save(loan);

        generatePaymentSchedule(loan);
        return loan;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Loan rejectLoan(Long loanId) {
        Loan loan = getLoanById(loanId);
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new IllegalStateException("Loan is not in PENDING state");
        }

        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan cancelLoan(Long loanId) {
        Loan loan = getLoanById(loanId);
        User currentUser = userService.getCurrentUser();

        if (!loan.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only cancel your own loans");
        }

        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new RuntimeException("Only pending loans can be cancelled");
        }

        loan.setStatus(Loan.LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    public Page<Loan> getUserLoans(Pageable pageable) {
        User user = userService.getCurrentUser();
        Page<Loan> loanPage = loanRepository.findByUser(user, pageable);
        return loanPage;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<Loan> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable);
    }

    public Loan getLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
    }

    @Transactional(readOnly = true)
    public LoanDetailResponse getLoanDetails(Long id) {
        Loan loan = getLoanById(id);
        User currentUser = userService.getCurrentUser();

        if (!loan.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ADMIN")) {
            throw new RuntimeException("You can only view your own loans");
        }

        List<Payment> payments = paymentRepository.findByLoan(loan);
        BigDecimal totalPaid = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PAID)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalInterestPaid = payments.stream()
                .filter(p -> p.getStatus() == Payment.PaymentStatus.PAID)
                .map(Payment::getInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = loan.getAmount().add(totalInterestPaid).subtract(totalPaid);

        totalPaid = totalPaid.add(totalInterestPaid);
        return LoanDetailResponse.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .status(loan.getStatus())
                .createdAt(loan.getCreatedAt())
                .approvedAt(loan.getApprovedAt())
                .completedAt(loan.getCompletedAt())
                .totalPaid(totalPaid)
                .remainingAmount(remainingAmount)
                .payments(payments.stream()
                        .map(p -> LoanDetailResponse.PaymentDetail.builder()
                                .id(p.getId())
                                .amount(p.getAmount())
                                .interest(p.getInterest())
                                .dueDate(p.getDueDate())
                                .paidAt(p.getPaidAt())
                                .installmentNumber(p.getInstallmentNumber())
                                .status(p.getStatus())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private void generatePaymentSchedule(Loan loan) {
        BigDecimal monthlyInterestRate = loan.getInterestRate().divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = calculateMonthlyPayment(loan.getAmount(), monthlyInterestRate, loan.getTermMonths());

        List<Payment> payments = new ArrayList<>();
        BigDecimal remainingBalance = loan.getAmount();
        LocalDateTime dueDate = LocalDateTime.now().plusMonths(1);

        for (int month = 1; month <= loan.getTermMonths(); month++) {
            BigDecimal interestPayment = remainingBalance.multiply(monthlyInterestRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalPayment = monthlyPayment.subtract(interestPayment).setScale(2, RoundingMode.HALF_UP);

            if (month == loan.getTermMonths()) {
                // Last payment - adjust for rounding
                principalPayment = remainingBalance;
                monthlyPayment = principalPayment.add(interestPayment);
            }

            Payment payment = Payment.builder()
                    .loan(loan)
                    .amount(monthlyPayment)
                    .interest(interestPayment)
                    .dueDate(dueDate)
                    .installmentNumber(month)
                    .status(Payment.PaymentStatus.PENDING)
                    .build();

            payments.add(payment);
            remainingBalance = remainingBalance.subtract(principalPayment).setScale(2, RoundingMode.HALF_UP);
            dueDate = dueDate.plusMonths(1);
        }

        // Verify the final balance is zero
        if (remainingBalance.compareTo(BigDecimal.ZERO) != 0) {
            // Adjust the last payment to ensure the balance is zero
            Payment lastPayment = payments.get(payments.size() - 1);
            lastPayment.setAmount(lastPayment.getAmount().add(remainingBalance));
            remainingBalance = BigDecimal.ZERO;
        }

        paymentRepository.saveAll(payments);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal monthlyRate, int termMonths) {
        // PMT = P × r × (1 + r)^n / ((1 + r)^n - 1)
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(termMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
} 