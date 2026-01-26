package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.ContactInquiry;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.service.ContactInquiryService;
import com.pms.propertymanagement.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactInquiryService inquiryService;
    private final RoomService roomService;

    @GetMapping("/send")
    public String sendInquiryGet(@RequestParam(required = false) Long roomId,
                                 @RequestParam(required = false) Long id) {
        Long targetId = roomId != null ? roomId : id;
        if (targetId == null) {
            return "redirect:/contact/received";
        }
        return "redirect:/contact/room/" + targetId;
    }

    @GetMapping
    public String index() {
        return "redirect:/contact/received";
    }

    @GetMapping("/room/{roomId}")
    public String showContactForm(@PathVariable Long roomId, Model model) {
        Room room = roomService.getById(roomId);
        model.addAttribute("room", room);
        model.addAttribute("inquiry", new ContactInquiry());
        return "contact/form";
    }

    @PostMapping("/room/{roomId}")
    public String sendInquiryForRoom(@PathVariable Long roomId,
                                     @Valid @ModelAttribute ContactInquiry inquiry,
                                     BindingResult bindingResult,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("room", roomService.getById(roomId));
            model.addAttribute("inquiry", inquiry);
            return "contact/form";
        }

        try {
            Room room = roomService.getById(roomId);
            inquiry.setRoom(room);
            inquiry.setProperty(room.getProperty());
            inquiry.setReceiver(room.getProperty().getOwner());
            inquiry.setStatus("PENDING");
            inquiryService.createInquiry(inquiry);
            redirectAttributes.addFlashAttribute("success", "Đã gửi yêu cầu liên hệ thành công!");
            return "redirect:/rooms/" + roomId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/contact/room/" + roomId;
        }
    }

    @PostMapping("/send")
    public String sendInquiry(@Valid @ModelAttribute ContactInquiry inquiry,
                              BindingResult bindingResult,
                              @RequestParam Long roomId,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        return sendInquiryForRoom(roomId, inquiry, bindingResult, model, redirectAttributes);
    }

    @GetMapping("/received")
    public String receivedInquiries(@RequestParam(required = false) Long hostId, Model model) {
        List<ContactInquiry> inquiries = new ArrayList<>();
        try {
            if (hostId != null) {
                inquiries = inquiryService.getInquiriesByReceiver(hostId);
            } else {
                inquiries = inquiryService.getAll();
            }
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải danh sách liên hệ. Hãy đảm bảo các cột text trong SQL Server là NVARCHAR (chạy scripts/sqlserver/unicode-columns.sql).");
        }

        int pendingCount = 0;
        for (ContactInquiry inquiry : inquiries) {
            if ("PENDING".equalsIgnoreCase(inquiry.getStatus())) {
                pendingCount++;
            }
        }

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("pageTitle", "Yêu cầu liên hệ nhận (Host)");
        model.addAttribute("hostId", hostId);
        return "contact/list";
    }

    @GetMapping("/{id}")
    public String viewInquiry(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("inquiry", inquiryService.getById(id));
            return "contact/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể mở chi tiết liên hệ.");
            return "redirect:/contact/received";
        }
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @RequestParam(required = false) Long hostId,
                               RedirectAttributes redirectAttributes) {
        try {
            inquiryService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật trạng thái!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        if (hostId != null) {
            return "redirect:/contact/received?hostId=" + hostId;
        }
        return "redirect:/contact/received";
    }
}
