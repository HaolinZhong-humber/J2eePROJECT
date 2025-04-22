package com.tmd.platform.service;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.Payment;
import com.tmd.platform.domain.User;
import com.tmd.platform.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanService loanService;
    private final UserService userService;

    public Page<Payment> getLoanPayments(Long loanId, Pageable pageable) {
        Loan loan = loanService.getLoanById(loanId);
        return paymentRepository.findByLoan(loan, pageable);
    }

    @Transactional
    public Payment makePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state");
        }

        // Get all payments for this loan
        List<Payment> paymentList = paymentRepository.findByLoan(payment.getLoan());

        // Check if there are any unpaid payments before this one
        boolean hasUnpaidPreviousPayments = paymentList.stream()
                .filter(p -> p.getInstallmentNumber() < payment.getInstallmentNumber())
                .anyMatch(p -> p.getStatus() != Payment.PaymentStatus.PAID);

        if (hasUnpaidPreviousPayments) {
            throw new IllegalStateException("Previous payments must be paid first");
        }

        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Page<Payment> getUpcomingPayments(Pageable pageable) {
        User user = userService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        return paymentRepository.findByDueDateAfterAndStatus(now, Payment.PaymentStatus.PENDING, pageable);
    }

    public Page<Payment> getOverduePayments(Pageable pageable) {
        User user = userService.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        return paymentRepository.findByDueDateBeforeAndStatus(now, Payment.PaymentStatus.PENDING, pageable);
    }
} 