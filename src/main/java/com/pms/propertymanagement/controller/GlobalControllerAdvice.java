package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.response.CategoryResponse;
import com.pms.propertymanagement.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final CategoryService categoryService;

    @ModelAttribute("categories")
    public List<CategoryResponse> getCategories() {
        try {
            return categoryService.findAll();
        } catch (Exception e) {
            return List.of();
        }
    }
}
