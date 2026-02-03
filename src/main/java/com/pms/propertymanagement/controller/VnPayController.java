package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.service.VnPayService;
import com.pms.propertymanagement.utils.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment/vnpay")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;

    @GetMapping("/return")
    public String vnpayReturn(HttpServletRequest request, RedirectAttributes ra) {
        Map<String, String> params = VnPayUtil.extractRequestParams(request.getParameterMap());
        String rawQuery = request.getQueryString();

        VnPayService.VnPayReturnResult result = vnPayService.handleReturn(params, rawQuery);

        if (!result.validSignature) {
            ra.addFlashAttribute("errorMessage", result.message);
            return "redirect:/owner/properties";
        }

        if (result.success) {
            ra.addFlashAttribute("successMessage", "Thanh Toán Thành Công");
        } else {
            ra.addFlashAttribute("errorMessage", result.message);
        }

        return "redirect:/owner/properties";
    }
}