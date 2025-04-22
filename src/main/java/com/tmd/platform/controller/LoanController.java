package com.tmd.platform.controller;

import com.tmd.platform.domain.Loan;
import com.tmd.platform.dto.LoanDetailResponse;
import com.tmd.platform.dto.LoanRequest;
import com.tmd.platform.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans", description = "Loan management APIs")
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    @Operation(summary = "Create a new loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan application created successfully",
            content = @Content(schema = @Schema(implementation = Loan.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Loan> createLoan(@Valid @RequestBody LoanRequest request) {
        return ResponseEntity.ok(loanService.createLoan(request));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's loans")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user's loans"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<Loan>> getMyLoans(Pageable pageable) {
        return ResponseEntity.ok(loanService.getUserLoans(pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all loans (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all loans"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<Loan>> getAllLoans(Pageable pageable) {
        return ResponseEntity.ok(loanService.getAllLoans(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get loan by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved loan details",
            content = @Content(schema = @Schema(implementation = LoanDetailResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<LoanDetailResponse> getLoan(@Parameter(description = "Loan ID") @PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanDetails(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a loan (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan approved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> approveLoan(@Parameter(description = "Loan ID") @PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a loan (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan rejected successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> rejectLoan(@Parameter(description = "Loan ID") @PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan cancelled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    public ResponseEntity<Loan> cancelLoan(@Parameter(description = "Loan ID") @PathVariable Long id) {
        return ResponseEntity.ok(loanService.cancelLoan(id));
    }
} 