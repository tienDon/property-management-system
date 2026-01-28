package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class AuthController {

    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/owner")
    public String login() {
        return "public/login";
    }

    @PostMapping("/owner")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session, Model model) {
        User  user = userService.authenticate(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            if (user.getRoles().stream().anyMatch(role -> role.getName().equals("OWNER"))) {
                return "redirect:/owner";
            }
        }

            model.addAttribute("error", "Sai gi do: " + username + " " + password);
            return "public/login";
    }

}
