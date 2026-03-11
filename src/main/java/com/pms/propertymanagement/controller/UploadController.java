package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

@Controller
public class UploadController {
    @Autowired
    private FileUploadService fileUploadService;
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }
    @PostMapping("/upload")
    public String upload(@RequestParam("frontImage") MultipartFile frontImage,
                         @RequestParam("backImage") MultipartFile backImage,
                         @RequestParam("faceImage") MultipartFile faceImage,
                         Model model) throws IOException {
        List<String> errors = new ArrayList<>();

        String frontHash = null;
        String backHash = null;
        String faceHash = null;

        try {
            frontHash = fileUploadService.uploadToVNPT(frontImage, "cccd front");
        } catch (Exception e) {
            errors.add("Upload CCCD mặt trước thất bại");
        }

        try {
            backHash = fileUploadService.uploadToVNPT(backImage, "cccd back");
        } catch (Exception e) {
            errors.add("Upload CCCD mặt sau thất bại");
        }

        try {
            faceHash = fileUploadService.uploadToVNPT(faceImage, "face");
        } catch (Exception e) {
            errors.add("Upload ảnh khuôn mặt thất bại");
        }

        model.addAttribute("frontHash", frontHash);
        model.addAttribute("backHash", backHash);
        model.addAttribute("faceHash", faceHash);
        model.addAttribute("errors", errors);

        return "result";
    }
}
