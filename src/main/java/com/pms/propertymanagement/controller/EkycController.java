package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.ekyc.EkycPageOutcome;
import com.pms.propertymanagement.dto.ekyc.EkycSubmitOutcome;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.EkycService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class EkycController {

    private final EkycService ekycService;

    @GetMapping("/ekyc")
    public String showEkyc(@RequestParam(value = "next", required = false) String next,
                           HttpSession session,
                           Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = ekycService.refreshUser(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute("user", user);

        EkycPageOutcome outcome = ekycService.buildPageOutcome(user, next);
        if (outcome.isRedirect()) {
            return "redirect:" + outcome.getRedirectUrl();
        }

        model.addAttribute("next", outcome.getNext());
        if (outcome.getLastSubmissionAt() != null) {
            model.addAttribute("lastSubmissionAt", outcome.getLastSubmissionAt());
        }
        return "public/ekyc";
    }

    @PostMapping("/ekyc")
    public String submitEkyc(@RequestParam("frontImage") MultipartFile frontImage,
                             @RequestParam("backImage") MultipartFile backImage,
                             @RequestParam("faceImage") MultipartFile faceImage,
                             @RequestParam(value = "next", required = false) String next,
                             HttpSession session,
                             Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        User user = ekycService.refreshUser(sessionUser.getId()).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/login";
        }
        EkycSubmitOutcome outcome = ekycService.submit(user, frontImage, backImage, faceImage, next);

        if (!outcome.isSuccess()) {
            model.addAttribute("errors", outcome.getErrors());
            model.addAttribute("next", outcome.getNext());
            return "public/ekyc";
        }

        session.setAttribute("user", outcome.getUser());
        return "redirect:" + outcome.getRedirectUrl();
    }
}
