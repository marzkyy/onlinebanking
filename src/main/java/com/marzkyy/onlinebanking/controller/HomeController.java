package com.marzkyy.onlinebanking.controller;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

@Controller
public class HomeController {
    private final UserService userService;

    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
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
        if (userBalance != null) {
            model.addAttribute("balance", userBalance.getAmount());
        } else {
            model.addAttribute("balance", BigDecimal.ZERO);
        }

        return "home"; // Render the home view
    }
}
