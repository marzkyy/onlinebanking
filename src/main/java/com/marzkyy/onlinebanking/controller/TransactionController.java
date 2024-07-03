package com.marzkyy.onlinebanking.controller;

import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.service.TransactionService;
import com.marzkyy.onlinebanking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;

@Controller
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping("/cash-in")
    public String cashInForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "cash-in";
    }

    @PostMapping("/cash-in")
    public String processCashIn(@ModelAttribute Transaction transaction, BindingResult bindingResult, HttpSession session) {
        // Retrieve the currently logged-in user
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        // Validate cash-in amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.addError(new FieldError("transaction", "amount", "Cash-in amount must be greater than zero."));
        }

        // If there are validation errors, return to the cash-in page
        if (bindingResult.hasErrors()) {
            return "cash-in";
        }

        // Process the cash-in
        transaction.setUser(loggedInUser);
        transactionService.processCashIn(transaction);

        log.info(">> Cash-in processed: {}", transaction);

        // Redirect to home page or another page after successful cash-in
        return "redirect:/home";
    }
}
