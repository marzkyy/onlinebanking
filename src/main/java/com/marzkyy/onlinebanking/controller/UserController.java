package com.marzkyy.onlinebanking.controller;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.marzkyy.onlinebanking.model.User;
import com.marzkyy.onlinebanking.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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

    @GetMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("/register")
    public String save(@ModelAttribute User user, BindingResult bindingResult){
        // Check if the email is already taken
        if(userService.userExists(user.getEmail())){
            bindingResult.addError(new FieldError("user", "email", "Email address already taken."));
        }
    
        // Check if the pin matches
        if(user.getPin() != null && user.getRpin() != null) {
            if(!user.getPin().equals(user.getRpin())){
                bindingResult.addError(new FieldError("user", "rpin", "PIN must match"));
            }
        }
    
        // Validate phone number format
        String phoneNumber = user.getNumber();
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            bindingResult.addError(new FieldError("user", "number", "Phone number is required"));
        } else if (!phoneNumber.matches("\\+?[0-9()\\s-]+")) {
            // Adjust the regex pattern as needed to match your phone number format requirements
            bindingResult.addError(new FieldError("user", "number", "Invalid phone number format"));
        }
    
        // If there are errors, return to the registration page
        if(bindingResult.hasErrors()){
            return "register";
        }
    
        // Save the user and redirect
        userService.save(user);
        log.info(">> User registered: {}", user);
        return "redirect:/login"; 
    }

    @GetMapping("/login")
    public String login(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute User user, BindingResult bindingResult, HttpSession session) {
        // Check if email and PIN are provided
        if (user.getEmail() == null || user.getPin() == null) {
            bindingResult.reject("credentials", "Email and PIN are required.");
            return "login";
        }

        // Verify if the email exists
        boolean emailExists = userService.userExists(user.getEmail());
        if (!emailExists) {
            bindingResult.addError(new FieldError("user", "email", "Email does not exist"));
            return "login";
        }

        // Retrieve user by email and verify PIN
        User foundUser = userService.findUserByEmail(user.getEmail())
                .orElse(null);

        if (foundUser == null || !foundUser.getPin().equals(user.getPin())) {
            bindingResult.addError(new FieldError("user", "pin", "PIN is incorrect"));
            return "login";
        }

        log.info(">> Logged in user: {}", foundUser);
        // Store the user in the session
        session.setAttribute("user", foundUser);

        // Redirect to home page
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("username", user.getName()); // Assuming getName() returns the user's name
        } else {
            model.addAttribute("username", "Guest"); // Fallback if no user is found in the session
        }
        return "home";
    }

 
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Redirect to login page
        return "redirect:/login";
    }

    @GetMapping("/changepin")
public String changePin(Model model) {
    model.addAttribute("user", new User()); // Add an empty user object to the model
    return "changepin";
}

@PostMapping("/changepin")
public String processChangePin(@ModelAttribute User user, BindingResult bindingResult, HttpSession session) {
    // Retrieve the currently logged-in user
    User loggedInUser = (User) session.getAttribute("user");

    if (loggedInUser == null) {
        return "redirect:/login"; // Redirect to login if no user is logged in
    }

    // Check if the current PIN is correct
    if (user.getPin() == null || !loggedInUser.getPin().equals(user.getPin())) {
        bindingResult.addError(new FieldError("user", "pin", "Current PIN is incorrect"));
        return "changepin";
    }

    // Check if the new PIN and confirmation PIN match
    if (user.getNpin() == null || !user.getNpin().equals(user.getRpin())) {
        bindingResult.addError(new FieldError("user", "rpin", "New PIN must match confirmation"));
        return "changepin";
    }

    // Update the user's PIN
    loggedInUser.setPin(user.getNpin()); // Set the new PIN
    userService.save(loggedInUser); // Save the updated user to the database

    log.info(">> User changed PIN: {}", loggedInUser);

    // Redirect to home page or another page after successful PIN change
    return "redirect:/home";
}
}
