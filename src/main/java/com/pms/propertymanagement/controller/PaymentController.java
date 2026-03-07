package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.service.WalletService;
import com.pms.propertymanagement.utils.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    
    private final WalletService walletService;
    
    @GetMapping("/wallet/return")
    public String handleWalletPaymentReturn(
            HttpServletRequest request, 
            RedirectAttributes ra) {
        
        try {
            Map<String, String> params = VnPayUtil.extractRequestParams(request.getParameterMap());
            String rawQuery = request.getQueryString();
            
            boolean success = walletService.processVnpayCallback(params, rawQuery);
            
            if (success) {
                ra.addFlashAttribute("successMessage", "Nạp tiền thành công!");
            } else {
                ra.addFlashAttribute("errorMessage", "Giao dịch không thành công hoặc bị hủy");
            }
            
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xử lý giao dịch: " + e.getMessage());
        }
        
        return "redirect:/owner/wallet";
    }
}