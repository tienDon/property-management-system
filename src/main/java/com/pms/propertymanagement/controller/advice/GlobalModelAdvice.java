package com.pms.propertymanagement.controller.advice;

import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.repository.PostingPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
    
    private final PostingPackageRepository postingPackageRepository;
    
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        // Only load posting packages for owner pages
        if (requestURI.startsWith("/owner")) {
            try {
                List<PostingPackage> postingPackages = postingPackageRepository.findAllByOrderByPriceAsc();
                model.addAttribute("postingPackages", postingPackages);
            } catch (Exception e) {
                // Log error but don't break the page
                model.addAttribute("postingPackages", List.of());
            }
        }
    }
}