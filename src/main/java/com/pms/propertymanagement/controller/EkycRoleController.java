package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.Role;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.AccountType;
import com.pms.propertymanagement.repository.RoleRepository;
import com.pms.propertymanagement.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class EkycRoleController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/ekyc/choose-role")
    public String showChooseRole(@RequestParam(value = "next", required = false) String next,
                                 HttpSession session,
                                 Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("user", user);

        if (!user.isEkycVerified()) {
            return "redirect:/ekyc?next=" + safeNextOrDefault(next, user);
        }

        if (user.getAccountType() != null) {
            return "redirect:" + safeNextOrDefault(next, user);
        }

        model.addAttribute("next", safeNextOrDefault(next, user));
        return "public/ekyc-choose-role";
    }

    @PostMapping("/ekyc/choose-role")
    public String chooseRole(@RequestParam("accountType") AccountType accountType,
                             @RequestParam(value = "next", required = false) String next,
                             HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = userRepository.findById(sessionUser.getId()).orElse(sessionUser);
        if (!user.isEkycVerified()) {
            return "redirect:/ekyc?next=" + safeNextOrDefault(next, user);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER chưa tồn tại"));
        Role ownerRole = roleRepository.findByName("OWNER")
                .orElseThrow(() -> new RuntimeException("Role OWNER chưa tồn tại"));

        user.getRoles().add(userRole);
        if (accountType == AccountType.OWNER || accountType == AccountType.BOTH) {
            user.getRoles().add(ownerRole);
        }

        user.setAccountType(accountType);
        User saved = userRepository.save(user);
        session.setAttribute("user", saved);

        return "redirect:" + safeNextOrDefault(next, saved);
    }

    private String safeNextOrDefault(String next, User user) {
        if (next != null && isValidRedirectUrl(next)) return next;
        boolean isOwner = user.getRoles().stream().anyMatch(r -> "OWNER".equals(r.getName()));
        return isOwner ? "/owner" : "/tenant/rooms";
    }

    private boolean isValidRedirectUrl(String url) {
        return url.startsWith("/") && !url.startsWith("//");
    }
}

