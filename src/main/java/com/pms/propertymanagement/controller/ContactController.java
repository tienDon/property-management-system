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

@Controller
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {
    private final ContactInquiryService inquiryService;
    private final RoomService roomService;

    /**
     * Hiển thị form liên hệ về 1 phòng
     * URL: /contact/room/1
     */
    @GetMapping("/room/{roomId}")
    public String showContactForm(@PathVariable Long roomId, Model model) {

        Room room = roomService.getById(roomId);
        model.addAttribute("room", room);

        // Object cho form
        ContactInquiry inquiry = new ContactInquiry();
        model.addAttribute("inquiry", inquiry);

        return "contact/form";
    }


    @PostMapping("/send")
    public String sendInquiry(@Valid @ModelAttribute ContactInquiry inquiry,
                              BindingResult bindingResult,
                              @RequestParam Long roomId,
                              Model model,
                              RedirectAttributes redirectAttributes) {


        if (bindingResult.hasErrors()) {
            Room room = roomService.getById(roomId);
            model.addAttribute("room", room);
            return "contact/form";
        }

        try {
            Room room = roomService.getById(roomId);

            inquiry.setRoom(room);
            inquiry.setProperty(room.getProperty());
            inquiry.setReceiver(room.getProperty().getOwner());
            inquiry.setStatus("PENDING");

            inquiryService.createInquiry(inquiry);

            redirectAttributes.addFlashAttribute(
                    "success", "Contact request sent successfully!"
            );

            return "redirect:/rooms/" + roomId;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error", "Error: " + e.getMessage()
            );
            return "redirect:/contact/room/" + roomId;
        }
    }

    /**
     * Admin / Host xem tất cả inquiry
     */
    @GetMapping("/all")
    public String allInquiries(Model model) {
        model.addAttribute("inquiries",
                inquiryService.getAll()); // cần thêm method này
        return "contact/list";
    }


    @GetMapping("/{id}")
    public String viewInquiry(@PathVariable Long id, Model model) {
        model.addAttribute("inquiry", inquiryService.getById(id));
        return "contact/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        inquiryService.updateStatus(id, status);
        redirectAttributes.addFlashAttribute("success", "Status updated!");
        return "redirect:/contact/all";
    }
}
