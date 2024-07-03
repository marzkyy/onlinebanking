package com.marzkyy.onlinebanking.controller;

import com.marzkyy.onlinebanking.model.Balance;
import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder){
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

        @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/register")
    public String register(@ModelAttribute User user) {
        return "register";
    }

    @PostMapping("/register")
    public String save(@ModelAttribute User user, BindingResult bindingResult){
        if(userService.userExists(user.getEmail())){
            bindingResult.addError(new FieldError("user", "email", "Email address already taken."));
        }

        if(user.getPin() != null && user.getRpin() != null) {
            if(!user.getPin().equals(user.getRpin())){
                bindingResult.addError(new FieldError("user", "rpin", "PIN must match"));
            }
        }

        String phoneNumber = user.getNumber();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            bindingResult.addError(new FieldError("user", "number", "Phone number is required"));
        } else if (!phoneNumber.matches("\\+?[0-9()\\s-]+")) {
            bindingResult.addError(new FieldError("user", "number", "Invalid phone number format"));
        }

        if(bindingResult.hasErrors()){
            return "register";
        }

        User savedUser = userService.save(user);

        log.info(">> User registered: {}", savedUser);
        return "redirect:/login"; 
    }

    @GetMapping("/login")
    public String login(@ModelAttribute User user) {
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute User user, BindingResult bindingResult, HttpSession session) {
        if (user.getEmail() == null || user.getPin() == null) {
            bindingResult.reject("credentials", "Email and PIN are required.");
            return "login";
        }

        boolean emailExists = userService.userExists(user.getEmail());
        if (!emailExists) {
            bindingResult.addError(new FieldError("user", "email", "Email does not exist"));
            return "login";
        }

        User foundUser = userService.findUserByEmail(user.getEmail()).orElse(null);
        if (foundUser == null || !foundUser.getPin().equals(user.getPin())) {
            bindingResult.addError(new FieldError("user", "pin", "PIN is incorrect"));
            return "login";
        }

        log.info(">> Logged in user: {}", foundUser);
        session.setAttribute("user", foundUser);

        return "redirect:/home";
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    @GetMapping("/changepin")
    public String changePin(Model model) {
        model.addAttribute("user", new User());
        return "changepin";
    }

    @PostMapping("/changepin")
    public String processChangePin(@ModelAttribute User user, BindingResult bindingResult, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        if (user.getPin() == null || !loggedInUser.getPin().equals(user.getPin())) {
            bindingResult.addError(new FieldError("user", "pin", "Current PIN is incorrect"));
            return "changepin";
        }

        if (user.getNpin() == null || !user.getNpin().equals(user.getRpin())) {
            bindingResult.addError(new FieldError("user", "rpin", "New PIN must match confirmation"));
            return "changepin";
        }

        loggedInUser.setPin(user.getNpin());
        userService.save(loggedInUser);

        log.info(">> User changed PIN: {}", loggedInUser);

        return "redirect:/home";
    }
}
