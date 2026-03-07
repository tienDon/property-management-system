package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ServiceItemRequest;
import com.pms.propertymanagement.dto.response.PropertyOwnerResponse;
import com.pms.propertymanagement.dto.response.ServiceItemResponse;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.ServiceItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/owner/services")
@RequiredArgsConstructor
public class ServiceItemController {

    private final ServiceItemService serviceItemService;
    private final PropertyService propertyService;

    @GetMapping
    public String index(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        // 1. Lấy danh sách dịch vụ đã tạo (để hiển thị bảng)
        List<ServiceItemResponse> services = serviceItemService.getAllServicesByOwner(user);
        model.addAttribute("services", services);

        model.addAttribute("content", "owner/service/list");
        model.addAttribute("activeMenu", "services");
        return "layout/owner-layout";
    }

    @GetMapping("/create")
    public String create(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";

        // 1. Lấy danh sách nhà trọ (để hiển thị dropdown chọn nhà)
        List<PropertyOwnerResponse> properties = propertyService.getPropertiesByOwner(user);
        model.addAttribute("properties", properties);

        // 2. Object binding cho form
        model.addAttribute("serviceRequest", new ServiceItemRequest());

        model.addAttribute("content", "owner/service/create");
        model.addAttribute("activeMenu", "properties");
        return "layout/owner-layout";
    }

    @PostMapping("/create")
    public String store(@ModelAttribute("serviceRequest") ServiceItemRequest request) {
        serviceItemService.createService(request);
        return "redirect:/owner/services?success";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        serviceItemService.deleteService(id);
        return "redirect:/owner/services?deleted";
    }
}
