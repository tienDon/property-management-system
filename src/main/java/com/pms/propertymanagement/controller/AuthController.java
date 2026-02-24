package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Authentication Controller - handles login and logout for all roles
 */
@Controller
@Slf4j
public class AuthController {

    private final UserService userService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ========== LOGIN ENDPOINTS ==========
    
    @GetMapping("/login/owner")
    public String showLoginForm() {
        return "public/login";
    }

    @PostMapping("/login/owner")
    public String login(@RequestParam("username") String username, 
                       @RequestParam("password") String password, 
                       HttpSession session, 
                       Model model) {
        User user = userService.authenticate(username, password);
        if (user != null) {
            session.setAttribute("user", user);
            if (user.getRoles().stream().anyMatch(role -> role.getName().equals("OWNER"))) {
                return "redirect:/owner";
            }
        }

        model.addAttribute("error", "Sai gi do: " + username + " " + password);
        return "public/login";
    }
    
    
    @GetMapping("/logout")
    public String logout(HttpSession session, 
                        @RequestParam(value = "redirect", required = false) String redirectUrl) {
        
        // Log logout event (optional, for security audit)
        if (session != null && session.getAttribute("user") != null) {
            log.info("User logout: {}", session.getAttribute("user"));
        }
        
        // Invalidate session completely (removes all attributes)
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to specified URL or default to owner login
        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            // Security: validate redirect URL to prevent open redirect vulnerability
            if (isValidRedirectUrl(redirectUrl)) {
                return "redirect:" + redirectUrl;
            } else {
                log.warn("Invalid redirect URL attempted: {}", redirectUrl);
            }
        }
        
        // Default redirect to owner login page
        return "redirect:/login/owner";
    }
    
    /**
     * Validate redirect URL to prevent open redirect attacks
     * Only allow internal paths (starting with /)
     */
    private boolean isValidRedirectUrl(String url) {
        // Allow only relative URLs starting with "/"
        // Reject external URLs (http://, https://, //)
        return url.startsWith("/") && !url.startsWith("//");
    }

}
