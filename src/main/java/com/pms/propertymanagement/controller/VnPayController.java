package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.service.VnPayService;
import com.pms.propertymanagement.service.WalletService;
import com.pms.propertymanagement.utils.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
    private final WalletService walletService;

    @GetMapping("/return")
    public String vnpayReturn(HttpServletRequest request, HttpSession session, RedirectAttributes ra) {
        Map<String, String> params = VnPayUtil.extractRequestParams(request.getParameterMap());
        String rawQuery = request.getQueryString();
        String txnRef = params.get("vnp_TxnRef");
        
        // Check if this is a wallet deposit or posting package payment based on txnRef prefix
        if (txnRef != null && txnRef.startsWith("WALLET_")) {
            // Handle wallet deposit
            boolean success = walletService.processVnpayCallback(params, rawQuery);
            
            if (success) {
                // Force refresh session userWallet after successful deposit
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    UserWallet updatedWallet = walletService.getOrCreateWallet(user);
                    session.setAttribute("userWallet", updatedWallet);
                    session.setAttribute("lastWalletUpdate", System.currentTimeMillis());
                }
                
                ra.addFlashAttribute("successMessage", "Nạp tiền vào ví thành công!");
            } else {
                ra.addFlashAttribute("errorMessage", "Nạp tiền thất bại. Vui lòng thử lại.");
            }
            
            return "redirect:/owner/wallet";
        } else {
            // Handle posting package payment
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
}