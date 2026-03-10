package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    // ========== TENANT / USER LOGIN ==========

    @GetMapping("/login")
    public String showUserLoginForm(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/";
        return "public/login-user";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            HttpSession session,
                            Model model) {
        User user = userService.authenticate(username, password);
        if (user == null) {
            model.addAttribute("error", "Sai username hoặc password");
            return "public/login-user";
        }
        session.setAttribute("user", user);
        String target = resolveRoleTarget(user);
        return "redirect:" + target;
    }

    // ========== OWNER / STAFF LOGIN ==========

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
            String target = resolveRoleTarget(user);
            return "redirect:" + target;
        }

        model.addAttribute("error", "Sai username hoặc password");
        return "public/login";
    }

    // ========== LOGOUT ==========

    @GetMapping("/logout")
    public String logout(HttpSession session,
                         @RequestParam(value = "redirect", required = false) String redirectUrl) {

        if (session != null) {
            session.invalidate();
        }

        if (redirectUrl != null && !redirectUrl.trim().isEmpty()) {
            if (isValidRedirectUrl(redirectUrl)) {
                return "redirect:" + redirectUrl;
            } else {
                log.warn("Invalid redirect URL attempted: {}", redirectUrl);
            }
        }

        return "redirect:/login/owner";
    }

    /**
     * Validate redirect URL to prevent open redirect attacks.
     * Only allow internal paths (starting with /).
     */
    private boolean isValidRedirectUrl(String url) {
        return url.startsWith("/") && !url.startsWith("//");
    }

    private boolean requiresEkyc(User user) {
        return user.getRoles().stream().anyMatch(r -> r.getName().equals("USER") || r.getName().equals("OWNER"));
    }

    private String resolveRoleTarget(User user) {
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("OWNER"))) {
            return "/owner";
        }
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("STAFF"))) {
            return "/staff/maintenance";
        }
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("MODERATOR"))) {
            return "/moderator";
        }
        return "/tenant/rooms";
    }
}
