package com.tmd.platform.repository;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Page<Loan> findByUser(User user, Pageable pageable);

    List<Loan> findByStatus(Loan.LoanStatus status);

    Page<Loan> findByUserAndStatus(User user, Loan.LoanStatus status, Pageable pageable);
} 