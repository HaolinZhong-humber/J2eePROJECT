package com.tmd.platform.service;

import com.tmd.platform.domain.BankCard;
import com.tmd.platform.domain.User;
import com.tmd.platform.dto.BankCardRequest;
import com.tmd.platform.dto.BankCardResponse;
import com.tmd.platform.exception.AppException;
import com.tmd.platform.repository.BankCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankCardService {
    private final BankCardRepository bankCardRepository;
    private final UserService userService;

    @Transactional
    public BankCardResponse addBankCard(BankCardRequest request) {
        User user = userService.getCurrentUser();

        // If this is the first card, set it as default
        if (!bankCardRepository.existsByUser(user)) {
            request.setDefault(true);
        }

        // If this card is set as default, unset other default cards
        if (request.isDefault()) {
            bankCardRepository.findByUserAndIsDefault(user, true)
                    .ifPresent(card -> {
                        card.setDefault(false);
                        bankCardRepository.save(card);
                    });
        }

        BankCard bankCard = BankCard.builder()
                .user(user)
                .bankName(request.getBankName())
                .cardNumber(request.getCardNumber())
                .cardHolderName(request.getCardHolderName())
                .expiryDate(request.getExpiryDate())
                .isDefault(request.isDefault())
                .build();

        bankCard = bankCardRepository.save(bankCard);
        return convertToResponse(bankCard);
    }

    public List<BankCardResponse> getUserBankCards() {
        User user = userService.getCurrentUser();
        return bankCardRepository.findByUser(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public BankCardResponse getDefaultBankCard() {
        User user = userService.getCurrentUser();
        return bankCardRepository.findByUserAndIsDefault(user, true)
                .map(this::convertToResponse)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "No default bank card found"));
    }

    private BankCardResponse convertToResponse(BankCard bankCard) {
        return BankCardResponse.builder()
                .id(bankCard.getId())
                .bankName(bankCard.getBankName())
                .maskedCardNumber(maskCardNumber(bankCard.getCardNumber()))
                .cardHolderName(bankCard.getCardHolderName())
                .expiryDate(bankCard.getExpiryDate())
                .createdAt(bankCard.getCreatedAt())
                .isDefault(bankCard.isDefault())
                .build();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
} 