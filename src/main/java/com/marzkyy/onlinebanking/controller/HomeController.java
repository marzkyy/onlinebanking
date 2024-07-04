package com.marzkyy.onlinebanking.controller;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.model.Transaction;
import com.marzkyy.onlinebanking.service.TransactionService;
import com.marzkyy.onlinebanking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    private final UserService userService;
    private final TransactionService transactionService;

    @Autowired
    public HomeController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GetMapping("/home")
    public String home(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect to login if no user is logged in
        }

        // Reload the user from the database to get the latest balance
        User updatedUser = userService.findById(loggedInUser.getId());
        session.setAttribute("user", updatedUser); // Update the session with the latest user data

        // Prepare user information for the view
        model.addAttribute("username", updatedUser.getName());

        Balance userBalance = updatedUser.getBalance();
        model.addAttribute("balance", userBalance != null ? userBalance.getAmount() : BigDecimal.ZERO);

        // Fetch and add transactions to the model
        List<Transaction> transactions = transactionService.getTransactionsForUser(updatedUser.getId());
        model.addAttribute("transactions", transactions);

        Long userId = loggedInUser.getId();
        model.addAttribute("userId", userId);

        return "home"; // Render the home view
    }
}
