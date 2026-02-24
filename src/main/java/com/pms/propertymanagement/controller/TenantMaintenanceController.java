package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.request.ContactRequest;
import com.pms.propertymanagement.entity.MaintenanceRequest;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.enums.MaintenanceCategory;
import com.pms.propertymanagement.enums.MaintenanceStatus;
import com.pms.propertymanagement.enums.RoomStatus;
import com.pms.propertymanagement.dto.request.MaintenanceCreateForm;
import com.pms.propertymanagement.service.ContactService;
import com.pms.propertymanagement.service.TenantMaintenanceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/tenant")
@RequiredArgsConstructor
public class TenantMaintenanceController {

    private final TenantMaintenanceService tenantMaintenanceService;
    private final ContactService contactService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<Room> rooms = tenantMaintenanceService.getAvailableRooms();
        model.addAttribute("rooms", rooms);
        model.addAttribute("content", "tenant/available-room-list");
        return "layout/public-main";
    }

    @PostMapping("/rooms/{roomId}/rent")
    public String rentRoom(@PathVariable Long roomId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        if (!StringUtils.hasText(user.getPhone())) {
            redirectAttributes.addFlashAttribute("rentError", "Vui lòng cập nhật số điện thoại trước khi gửi yêu cầu thuê.");
            return "redirect:/tenant/home";
        }

        Room room = tenantMaintenanceService.getRoomDetail(roomId);
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            redirectAttributes.addFlashAttribute("rentError", "Phòng này không còn trống.");
            return "redirect:/tenant/home";
        }
        if (room.getProperty() == null || !StringUtils.hasText(room.getProperty().getSlug())) {
            redirectAttributes.addFlashAttribute("rentError", "Không tìm thấy bài đăng của phòng này.");
            return "redirect:/tenant/home";
        }

        ContactRequest contact = new ContactRequest();
        contact.setName(StringUtils.hasText(user.getFullName()) ? user.getFullName() : user.getUsername());
        contact.setPhone(user.getPhone());
        contact.setNote("Yêu cầu thuê phòng: " + room.getName() + " (roomId=" + room.getId() + ")");

        contactService.createContact(room.getProperty().getSlug(), contact);
        redirectAttributes.addFlashAttribute("rentSuccess", true);
        return "redirect:/tenant/home";
    }

    @GetMapping("/rooms")
    public String listRooms(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<Room> rooms = tenantMaintenanceService.getRoomsForTenant(user);
        model.addAttribute("rooms", rooms);
        model.addAttribute("content", "tenant/room-list");
        return "layout/public-main";
    }

    @GetMapping("/rooms/{roomId}")
    public String roomDetail(@PathVariable Long roomId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        Room room = tenantMaintenanceService.getRoomDetail(roomId);
        boolean canRequestMaintenance = tenantMaintenanceService.isRoomRentedByTenant(roomId, user);
        model.addAttribute("room", room);
        model.addAttribute("canRequestMaintenance", canRequestMaintenance);
        model.addAttribute("content", "tenant/room-detail");
        return "layout/public-main";
    }

    @GetMapping("/rooms/{roomId}/maintenance/create")
    public String showCreateForm(@PathVariable Long roomId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (!tenantMaintenanceService.isRoomRentedByTenant(roomId, user)) {
            return "redirect:/tenant/rooms/" + roomId;
        }
        Room room = tenantMaintenanceService.getRoomDetail(roomId);
        model.addAttribute("room", room);
        model.addAttribute("maintenanceCategories", Arrays.asList(MaintenanceCategory.values()));
        MaintenanceCreateForm form = new MaintenanceCreateForm();
        Object cat = model.asMap().get("formCategory");
        Object desc = model.asMap().get("formDescription");
        if (cat instanceof MaintenanceCategory) {
            form.setCategory((MaintenanceCategory) cat);
        } else if (cat instanceof String && StringUtils.hasText((String) cat)) {
            try {
                form.setCategory(MaintenanceCategory.valueOf(((String) cat).trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (desc instanceof String) {
            form.setDescription((String) desc);
        }
        model.addAttribute("maintenanceForm", form);
        model.addAttribute("content", "tenant/maintenance-create");
        return "layout/public-main";
    }

    @GetMapping("/maintenance")
    public String listMaintenance(HttpSession session, Model model,
                                  @RequestParam(name = "success", required = false) String success,
                                  @RequestParam(name = "createdId", required = false) Long createdId) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        List<MaintenanceRequest> requests = tenantMaintenanceService.getRequestsForTenant(user);
        model.addAttribute("requests", requests);
        model.addAttribute("success", success != null);
        model.addAttribute("createdId", createdId);
        model.addAttribute("content", "tenant/maintenance-list");
        return "layout/public-main";
    }

    @PostMapping("/rooms/{roomId}/maintenance/create")
    public String createRequest(@PathVariable Long roomId,
                                @RequestParam(name = "category", required = false) String categoryRaw,
                                @RequestParam(name = "description", required = false) String description,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        boolean hasError = false;
        MaintenanceCategory category = null;
        if (StringUtils.hasText(categoryRaw)) {
            try {
                category = MaintenanceCategory.valueOf(categoryRaw.trim());
            } catch (IllegalArgumentException ex) {
                redirectAttributes.addFlashAttribute("errorCategory", "Danh mục không hợp lệ");
                hasError = true;
            }
        }
        if (category == null) {
            redirectAttributes.addFlashAttribute("errorCategory", "Vui lòng chọn danh mục");
            hasError = true;
        }
        if (description == null || description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorDescription", "Vui lòng nhập mô tả");
            hasError = true;
        }
        redirectAttributes.addFlashAttribute("formCategory", categoryRaw);
        redirectAttributes.addFlashAttribute("formDescription", description);
        if (hasError) {
            return "redirect:/tenant/rooms/" + roomId + "/maintenance/create";
        }

        MaintenanceRequest saved = tenantMaintenanceService.createRequest(roomId, user, category, description);
        return "redirect:/tenant/maintenance?success=true&createdId=" + saved.getId();
    }

    @GetMapping("/maintenance/{id}")
    public String requestDetail(@PathVariable Long id, HttpSession session, Model model,
                                @RequestParam(name = "success", required = false) String success) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        MaintenanceRequest req = tenantMaintenanceService.getTenantRequestDetail(id, user);
        model.addAttribute("request", req);
        model.addAttribute("success", success != null);
        model.addAttribute("statuses", MaintenanceStatus.values());
        model.addAttribute("content", "tenant/maintenance-detail");
        return "layout/public-main";
    }

    @PostMapping("/maintenance/{id}/confirm")
    public String confirm(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        tenantMaintenanceService.confirmCompletion(id, user);
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/tenant/maintenance/" + id;
    }

    @PostMapping("/maintenance/{id}/reopen")
    public String reopen(@PathVariable Long id,
                         @RequestParam(name = "reason", required = false) String reason,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorReopen", "Vui lòng nhập lý do");
            return "redirect:/tenant/maintenance/" + id;
        }
        tenantMaintenanceService.reopenRequest(id, reason, user);
        redirectAttributes.addAttribute("success", "true");
        return "redirect:/tenant/maintenance/" + id;
    }
}

