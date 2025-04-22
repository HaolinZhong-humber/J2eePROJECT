package com.tmd.platform.dto;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.domain.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoanDetailResponse {
    private Long id;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private Loan.LoanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;
    private BigDecimal totalPaid;
    private BigDecimal remainingAmount;
    private List<PaymentDetail> payments;

    @Data
    @Builder
    public static class PaymentDetail {
        private Long id;
        private BigDecimal amount;
        private BigDecimal interest;
        private BigDecimal interestRate;
        private LocalDateTime dueDate;
        private LocalDateTime paidAt;
        private Integer installmentNumber;
        private Payment.PaymentStatus status;
    }
} 