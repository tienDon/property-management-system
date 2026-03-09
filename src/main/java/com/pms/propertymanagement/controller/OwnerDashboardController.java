package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.OwnerStatisticsDTO;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.StatisticsService;
import com.pms.propertymanagement.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerDashboardController {

    private final StatisticsService statisticsService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(
            Model model, 
            HttpSession session,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login/owner";
        }
        
        user = userService.findById(user.getId()).orElse(user);
        
        OwnerStatisticsDTO stats = statisticsService.getOwnerStatistics(user.getId());
        model.addAttribute("stats", stats);
        
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        
        model.addAttribute("incomeChartData", statisticsService.getOwnerIncomeChart(user.getId(), startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        
        // Add Recent Activities
        model.addAttribute("recentActivities", statisticsService.getRecentActivities(user.getId()));
        
        model.addAttribute("content", "owner/dashboard");
        return "layout/owner-layout";
    }
}
