package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.TenantRequest;
import com.pms.propertymanagement.dto.response.TenantResponse;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.TenantService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/owner/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping
    public String index(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login/owner";
        
        List<TenantResponse> tenants = tenantService.getAllTenantsByOwner(sessionUser);
        model.addAttribute("tenants", tenants);
        model.addAttribute("content", "owner/tenant/list");
        model.addAttribute("activeMenu", "tenants");
        return "layout/owner-layout";
    }

    @GetMapping("/create")
    public String create(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login/owner";

        model.addAttribute("tenantRequest", new TenantRequest());
        model.addAttribute("content", "owner/tenant/create");
        model.addAttribute("activeMenu", "tenants");
        return "layout/owner-layout";
    }

    @PostMapping("/create")
    public String store(@ModelAttribute("tenantRequest") TenantRequest request, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login/owner";
        
        tenantService.createTenant(request, sessionUser);

        return "redirect:/owner/tenants?success";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return "redirect:/owner/tenants?deleted";
    }
}
