package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.MaintenanceWorkflowService;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/maintenance")
@RequiredArgsConstructor
public class OwnerMaintenanceController {
    private final MaintenanceWorkflowService maintenanceWorkflowService;
    private final UserService userService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        List<MaintenanceRequest> requests = maintenanceWorkflowService.getRequestsForOwner(user);
        model.addAttribute("requests", requests);
        model.addAttribute("content", "owner/maintenance/list");
        model.addAttribute("activeMenu", "maintenance");
        return "layout/owner-layout";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        MaintenanceRequest req = maintenanceWorkflowService.getRequestDetailForOwner(id, user);
        model.addAttribute("request", req);
        model.addAttribute("staffs", userService.getUsersByRole("STAFF"));
        model.addAttribute("content", "owner/maintenance/detail");
        model.addAttribute("activeMenu", "maintenance");
        return "layout/owner-layout";
    }

    @PostMapping("/{id}/assign")
    public String assign(@PathVariable Long id,
                         @RequestParam(name = "staffId", required = false) Long staffId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        try {
            maintenanceWorkflowService.assignRequest(id, user, staffId);
            redirectAttributes.addAttribute("success", "true");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorAssign", ex.getMessage());
        }
        return "redirect:/owner/maintenance/" + id;
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(name = "reason", required = false) String reason,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        try {
            maintenanceWorkflowService.rejectRequest(id, user, StringUtils.hasText(reason) ? reason.trim() : null);
            redirectAttributes.addAttribute("success", "true");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorReject", ex.getMessage());
            redirectAttributes.addFlashAttribute("rejectReason", reason);
        }
        return "redirect:/owner/maintenance/" + id;
    }
}
