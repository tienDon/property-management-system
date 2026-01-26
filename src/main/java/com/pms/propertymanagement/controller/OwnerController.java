package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.response.PropertyResponse;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.CategoryRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.repository.ProvinceRepository;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.utils.DateUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.util.DateUtils;

import java.util.List;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final PropertyService propertyService;
    public OwnerController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping("/properties")
    public String properties(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<PropertyResponse> properties = propertyService.getPropertiesByOwner(user);



        model.addAttribute("properties", properties);
        return "owner/properties";
    }

    @GetMapping("/properties/create")
    public String  create(Model model) {
        model.addAttribute("provinces", provinceRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        return "owner/property-form";
    }


}
