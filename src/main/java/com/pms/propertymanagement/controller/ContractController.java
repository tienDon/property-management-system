package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContractRequest;
import com.pms.propertymanagement.entity.Contract;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.ContractStatus;
// Removed RoomStatus
import com.pms.propertymanagement.service.ContractService;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.RoomService;
import com.pms.propertymanagement.service.TenantService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.pms.propertymanagement.dto.response.RoomResponse;

@Controller
@RequestMapping("/owner/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final PropertyService propertyService;
    private final RoomService roomService;
    private final TenantService tenantService;

    @GetMapping
    public String listContracts(Model model, HttpSession session,
                                @RequestParam(required = false) ContractStatus status,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner"; // Just in case

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Contract> contractPage = contractService.getContractsByOwner(user.getId(), status, keyword, pageable);

        model.addAttribute("contracts", contractPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status); // For tab active state
        
        model.addAttribute("content", "owner/contract/list");

        model.addAttribute("activeMenu", "contracts");
        return "layout/owner-layout";
    }

    @GetMapping("/create")
    public String createContractPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        model.addAttribute("properties", propertyService.getPropertiesByOwner(user));
        model.addAttribute("tenants", tenantService.getAllTenantsByOwner(user));
        model.addAttribute("content", "owner/contract/create");

        model.addAttribute("activeMenu", "contracts");
        return "layout/owner-layout";
    }

    @PostMapping("/create")
    public String createContract(@ModelAttribute ContractRequest contractRequest, HttpSession session) {
        contractService.createContract(contractRequest);
        return "redirect:/owner/contracts?success=true";
    }

    @PostMapping("/{id}/terminate")
    public String terminateContract(@PathVariable Long id) {
        contractService.updateContractStatus(id, ContractStatus.TERMINATED);
        return "redirect:/owner/contracts";
    }

    // AJAX helper endpoints
    @GetMapping("/api/rooms-by-property")
    @ResponseBody
    public List<RoomResponse> getRoomsByProperty(@RequestParam Long propertyId) {
        return roomService.getAvailableRoomsByProperty(propertyId);
    }
}
