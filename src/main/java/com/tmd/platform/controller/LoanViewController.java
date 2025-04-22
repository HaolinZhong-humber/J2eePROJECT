package com.tmd.platform.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoanViewController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/loans/my")
    public String myLoans() {
        return "loan/list";
    }

    @GetMapping("/loans/apply")
    public String applyLoan() {
        return "loan/apply";
    }

    @GetMapping("/loans/{id}")
    public String loanDetail() {
        return "loan/detail";
    }
} 