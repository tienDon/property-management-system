package com.pms.propertymanagement.controller;


import com.pms.propertymanagement.dto.request.RoomSearchRequest;
import com.pms.propertymanagement.dto.response.RoomSearchResponse;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.service.CategoryService;
import com.pms.propertymanagement.service.ProvinceService;
import com.pms.propertymanagement.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final CategoryService categoryService;
    private final ProvinceService provinceService;

    @GetMapping("/detail/{id}")
    public String getRoomDetail(@PathVariable Long id, Model model) {
        Room room = roomService.getById(id);
        model.addAttribute("room", room);
        return "rooms/detail";
    }

    @GetMapping()
    public String getAllRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("provinces", provinceService.findAllProvince());
        model.addAttribute("categories", categoryService.findAllCategory());
        return "rooms/list";
    }

    @GetMapping("/search")
    public String searchRooms(
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String districtCode,
            @RequestParam(required = false) String wardCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea,
            Model model
    ) {
        model.addAttribute("rooms", roomService.searchRooms(
                provinceCode, districtCode, wardCode,
                categoryId, minPrice, maxPrice,
                minArea, maxArea
        ));

        model.addAttribute("provinces", provinceService.findAllProvince());
        model.addAttribute("categories", categoryService.findAllCategory());

        model.addAttribute("provinceCode", provinceCode);
        model.addAttribute("districtCode", districtCode);
        model.addAttribute("wardCode", wardCode);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minArea", minArea);
        model.addAttribute("maxArea", maxArea);

        return "rooms/list";
    }
}
