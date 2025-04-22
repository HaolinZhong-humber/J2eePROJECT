package com.tmd.platform.repository;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByLoan(Loan loan, Pageable pageable);

    List<Payment> findByLoan(Loan loan);

    List<Payment> findByLoanAndStatus(Loan loan, Payment.PaymentStatus status);

    Page<Payment> findByDueDateBeforeAndStatus(LocalDateTime date, Payment.PaymentStatus status, Pageable pageable);

    Page<Payment> findByDueDateAfterAndStatus(LocalDateTime date, Payment.PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.loan.user.id = :id")
    List<Payment> findBUserId(Long id);
}