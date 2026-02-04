package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.dto.response.PropertyDetailResponse;
import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.service.CategoryService;
import com.pms.propertymanagement.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PublicController {


    private final CategoryService categoryService;

    private final PropertyService propertyService;

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "public/home");
        model.addAttribute("properties", propertyService.getAll());

        return "layout/public-main";
    }

    @GetMapping("/public/category/{id}")
    public String listByCategory(@PathVariable("id") Long categoryId, Model model) {

        List<PropertyResponse> properties = propertyService.getPropertiesByCategory(categoryId);

        model.addAttribute("properties", properties);
        model.addAttribute("currentCategoryId", categoryId);

        // Vẫn cần các list phụ cho sidebar/search
        model.addAttribute("allAmenities", propertyService.getAllAmenities());
        model.addAttribute("categoryId", categoryId);

        model.addAttribute("content", "public/property-list");
        return "layout/public-main";
    }

    @GetMapping("/public/property/{slug}")
    public String propertyDetail(@PathVariable("slug") String slug, Model model) {
        // 1. Gọi service lấy dữ liệu chi tiết qua slug
        PropertyDetailResponse property = propertyService.getPropertyDetailBySlug(slug);

        // 2. Đưa vào model
        model.addAttribute("p", property);

        // 3. Layout fragment (giống các trang trước bạn làm)
        model.addAttribute("content", "public/property-detail");

        model.addAttribute("contact", new ContactRequest());

        return "layout/public-main";
    }

}
