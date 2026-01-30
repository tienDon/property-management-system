package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.PropertyRequest;
import com.pms.propertymanagement.dto.response.PropertyOwnerResponse;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.CategoryService;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
        
import java.util.List;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final PropertyService propertyService;
    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        // Redirect to properties for now or show a dashboard page
        model.addAttribute("content", "owner/dashboard");
        return "layout/owner-layout";
    }

    @GetMapping("/properties")
    public String listProperties(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        // Refresh user from DB if strictly needed, or just use session user
        // user = userService.findByUsername(user.getUsername()); 
        
        List<PropertyOwnerResponse> properties = propertyService.getPropertiesByOwner(user);
        model.addAttribute("properties", properties);
        model.addAttribute("activeMenu", "properties");
        
        model.addAttribute("content", "owner/property/list");
        return "layout/owner-layout";
    }

    @GetMapping("/properties/create")
    public String showCreatePropertyForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        model.addAttribute("propertyRequest", new PropertyRequest());
        model.addAttribute("actionUrl", "/owner/properties/create");
        
        // Pass reference data via Services
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("amenities", propertyService.getAllAmenities());
        model.addAttribute("provinces", propertyService.getAllProvinces());
        model.addAttribute("surroundings", propertyService.getAllSurroundings());
        model.addAttribute("targetTenants", propertyService.getAllTargetTenants());
        
        model.addAttribute("content", "owner/property/create");
        model.addAttribute("activeMenu", "properties");
        return "layout/owner-layout";
    }

    @PostMapping("/properties/create")
    public String createProperty(@ModelAttribute("propertyRequest") PropertyRequest propertyRequest, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        propertyService.createProperty(propertyRequest, user);
        return "redirect:/owner/properties";
    }

    @GetMapping("/properties/edit/{id}")
    public String showEditPropertyForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        model.addAttribute("propertyRequest", propertyService.getPropertyForEdit(id));
        model.addAttribute("actionUrl", "/owner/properties/edit/" + id);
        
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("amenities", propertyService.getAllAmenities());
        model.addAttribute("provinces", propertyService.getAllProvinces());
        model.addAttribute("surroundings", propertyService.getAllSurroundings());
        model.addAttribute("targetTenants", propertyService.getAllTargetTenants());
        
        model.addAttribute("content", "owner/property/create"); // Re-using create form - Note: naming should probably be form.html
        return "layout/owner-layout";
    }

    @PostMapping("/properties/edit/{id}")
    public String updateProperty(@PathVariable Long id, @ModelAttribute("propertyRequest") PropertyRequest propertyRequest, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        propertyService.updateProperty(id, propertyRequest);
        return "redirect:/owner/properties";
    }

    @PostMapping("/properties/delete/{id}")
    public String deleteProperty(@PathVariable Long id, HttpSession session) {
         User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        propertyService.deleteProperty(id);
        return "redirect:/owner/properties";
    }
}
