package com.marzkyy.onlinebanking.controller;

import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.service.TransactionService;
import com.marzkyy.onlinebanking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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

    // Method to display cash-in form
    @GetMapping("/cash-in")
    public String cashInForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "cash-in";
    }

    // Method to process cash-in
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

    // Method to display cash-out form
    @GetMapping("/cash-out")
    public String cashOutForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "cash-out";
    }

    // Method to process cash-out
    @PostMapping("/cash-out")
    public String processCashOut(@ModelAttribute Transaction transaction, BindingResult bindingResult, HttpSession session) {
        // Retrieve the currently logged-in user
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        // Validate cash-out amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.addError(new FieldError("transaction", "amount", "Cash-out amount must be greater than zero."));
        }

        // If there are validation errors, return to the cash-out page
        if (bindingResult.hasErrors()) {
            return "cash-out";
        }

        // Process the cash-out
        transaction.setUser(loggedInUser);

        try {
            transactionService.processCashOut(transaction);
            log.info(">> Cash-out processed: {}", transaction);
        } catch (RuntimeException e) {
            // Handle the exception and add an error message
            bindingResult.rejectValue("amount", "insufficient.funds", e.getMessage());
            return "cash-out";
        }

        // Redirect to home page or another page after successful cash-out
        return "redirect:/home";
    }

    // Method to display transfer form
    @GetMapping("/cash-transfer")
    public String transferForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        return "cash-transfer";
    }

    // Method to process transfer
    @PostMapping("/cash-transfer")
    public String processTransfer(@ModelAttribute Transaction transaction, BindingResult bindingResult, HttpSession session) {
        // Retrieve the currently logged-in user
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        // Validate transfer amount
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.addError(new FieldError("transaction", "amount", "Transfer amount must be greater than zero."));
        }

        // Validate recipient
        if (transaction.getTransferTo() == null || transaction.getTransferTo().getId() == null) {
            bindingResult.addError(new FieldError("transaction", "transferTo", "Recipient must be specified."));
        }

        // Validate that recipient is not the same as sender
        if (transaction.getTransferTo() != null && transaction.getTransferTo().getId().equals(loggedInUser.getId())) {
            bindingResult.addError(new FieldError("transaction", "transferTo", "Cannot transfer to yourself."));
        }

        // If there are validation errors, return to the transfer page
        if (bindingResult.hasErrors()) {
            return "cash-transfer";
        }

        // Set the sender for the transaction
        transaction.setTransferFrom(loggedInUser);

        try {
            // Process the transfer
            transactionService.processCashTransfer(transaction);
            log.info(">> Transfer processed: {}", transaction);
        } catch (RuntimeException e) {
            // Handle the exception and add an error message
            bindingResult.rejectValue("amount", "insufficient.funds", e.getMessage());
            return "cash-transfer";
        }

        // Redirect to home page or another page after successful transfer
        return "redirect:/home";
    }
}
