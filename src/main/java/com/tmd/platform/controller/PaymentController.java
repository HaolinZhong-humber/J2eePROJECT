package com.tmd.platform.controller;

import com.tmd.platform.domain.Payment;
import com.tmd.platform.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor

@Tag(name = "Payments", description = "Payment management APIs")
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping("/loan/{loanId}")
    @Operation(summary = "Get payments for a loan")
    public ResponseEntity<Page<Payment>> getLoanPayments(
            @PathVariable Long loanId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.getLoanPayments(loanId, pageable));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Make a payment")
    public ResponseEntity<Payment> makePayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.makePayment(id));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming payments")
    public ResponseEntity<Page<Payment>> getUpcomingPayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getUpcomingPayments(pageable));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue payments")
    public ResponseEntity<Page<Payment>> getOverduePayments(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getOverduePayments(pageable));
    }
} 