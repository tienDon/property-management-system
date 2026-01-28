package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.RoomRequest;
import com.pms.propertymanagement.dto.response.PropertyOwnerResponse;
import com.pms.propertymanagement.dto.response.RoomResponse;
import com.pms.propertymanagement.dto.response.ServiceItemResponse;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.service.PropertyService;
import com.pms.propertymanagement.service.RoomService;
import com.pms.propertymanagement.service.ServiceItemService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PropertyService propertyService;
    private final ServiceItemService serviceItemService;

    @GetMapping
    public String listRooms(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<RoomResponse> rooms = roomService.getAllRoomsByOwner(user);
        model.addAttribute("rooms", rooms);
        model.addAttribute("content", "owner/room/list");
        model.addAttribute("activeMenu", "rooms");
        return "layout/owner-layout";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<PropertyOwnerResponse> properties = propertyService.getPropertiesByOwner(user);
        
        model.addAttribute("roomRequest", new RoomRequest());
        model.addAttribute("properties", properties);
        model.addAttribute("content", "owner/room/create");
        model.addAttribute("activeMenu", "rooms");
        return "layout/owner-layout";
    }

    @PostMapping("/create")
    public String createRoom(@ModelAttribute("roomRequest") RoomRequest roomRequest,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        try {
            roomService.createRoom(roomRequest, user);
            redirectAttributes.addAttribute("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", true);
            return "redirect:/owner/rooms/create";
        }
        return "redirect:/owner/rooms";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        roomService.deleteRoom(id);
        redirectAttributes.addAttribute("deleted", true);
        return "redirect:/owner/rooms";
    }
    
    // API to get services by property ID (for AJAX in create form)
    @GetMapping("/api/properties/{propertyId}/services")
    @ResponseBody
    public List<ServiceItemResponse> getServicesByProperty(@PathVariable Long propertyId) {
        return serviceItemService.getServicesByPropertyId(propertyId);
    }
}
