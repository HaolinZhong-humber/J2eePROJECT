package com.tmd.platform.controller;

import com.tmd.platform.dto.BankCardRequest;
import com.tmd.platform.dto.BankCardResponse;
import com.tmd.platform.service.BankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bank-cards")
@RequiredArgsConstructor
@Tag(name = "Bank Cards", description = "Bank card management APIs")
public class BankCardController {
    private final BankCardService bankCardService;

    @PostMapping
    @Operation(summary = "Add a new bank card")
    public ResponseEntity<BankCardResponse> addBankCard(@RequestBody BankCardRequest request) {
        return ResponseEntity.ok(bankCardService.addBankCard(request));
    }

    @GetMapping
    @Operation(summary = "Get user's bank cards")
    public ResponseEntity<List<BankCardResponse>> getUserBankCards() {
        return ResponseEntity.ok(bankCardService.getUserBankCards());
    }

    @GetMapping("/default")
    @Operation(summary = "Get user's default bank card")
    public ResponseEntity<BankCardResponse> getDefaultBankCard() {
        return ResponseEntity.ok(bankCardService.getDefaultBankCard());
    }
} 