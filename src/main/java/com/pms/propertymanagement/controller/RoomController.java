package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/{id}")
    public String getRoomDetail(@PathVariable Long id, Model model) {
        Room room = roomService.getById(id);
        model.addAttribute("room", room);
        return "rooms/detail";
    }
}
