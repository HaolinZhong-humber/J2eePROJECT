package com.tmd.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BankCardViewController {

    @GetMapping("/bank-cards/add")
    public String addBankCard() {
        return "bank-card/add";
    }
} 