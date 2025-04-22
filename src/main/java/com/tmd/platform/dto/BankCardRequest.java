package com.tmd.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BankCardRequest {
    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{16,19}$", message = "Invalid card number")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    @NotBlank(message = "Expiry date is required")
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Invalid expiry date format (MM/YY)")
    private String expiryDate;

    private boolean isDefault;
} 