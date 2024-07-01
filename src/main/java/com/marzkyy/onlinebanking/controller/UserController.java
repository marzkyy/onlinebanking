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
    public String save(User user, BindingResult bindingResult){
        //check if the email already taken
        if(userService.userExists(user.getEmail())){
            bindingResult.addError(new FieldError("user","email","Email address already taken."));
        }

        //check if the pin match
        if(user.getPin() != null && user.getRpin() != null)
        {
            if(!user.getPin().equals(user.getRpin())){
                bindingResult.addError(new FieldError("user", "rpin", "PIN must match"));
            }
        }

        if(bindingResult.hasErrors()){
            return "register";
        }
        userService.save(user);
        log.info(">> user : {}", user.toString());
        return "redirect:/login"; 
    }

    @GetMapping("/login")
    public String login(@ModelAttribute User user, Model model) {
        model.addAttribute("user", user);
        return "login";
    }

    @PostMapping("/login")
    public String authenticate(@ModelAttribute User user, BindingResult bindingResult, Model model) {
        // Check if email is provided
        if (user.getEmail() == null || user.getPin() == null) {
            bindingResult.reject("credentials", "Email and PIN are required.");
            return "login";
        }

        // Verify if the email exists
        boolean emailExists = userService.userExists(user.getEmail());
        if (!emailExists) {
            bindingResult.addError(new FieldError("user", "email", "Email does not exists"));
            return "login";
        }

        // Retrieve user by email and verify PIN
        User foundUser = userService.findUserByEmail(user.getEmail())
                .orElse(null);

        if (foundUser == null || !foundUser.getPin().equals(user.getPin())) {
            bindingResult.addError(new FieldError("user", "pin", "Password is incorrect"));
            return "login";
        }

        log.info(">> Logged in user : {}", foundUser.toString());
        model.addAttribute("user", foundUser);

        // Redirect to a home page or dashboard
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        // For example, adding a dummy user to the model.
        // Ideally, this should come from a session or authentication context.
        User dummyUser = new User();
        dummyUser.setName("User"); // Replace this with actual user data
        model.addAttribute("user", dummyUser);
        return "home";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
public String logout(HttpServletRequest request, HttpServletResponse response) {
    // Invalidate the session
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }

    // Redirect to login page or any other page
    return "redirect:/login";
}
  
}