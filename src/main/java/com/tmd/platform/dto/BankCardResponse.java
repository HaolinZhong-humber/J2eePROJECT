package com.tmd.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankCardResponse {
    private Long id;
    private String bankName;
    private String maskedCardNumber;
    private String cardHolderName;
    private String expiryDate;
    private LocalDateTime createdAt;
    private boolean isDefault;
} 