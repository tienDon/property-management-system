package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.entity.WalletTransaction;
import com.pms.propertymanagement.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/owner/wallet")
@RequiredArgsConstructor
public class WalletController {
    
    private final WalletService walletService;
    
    @GetMapping
    public String walletDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        UserWallet wallet = walletService.getOrCreateWallet(user);
        
        // Wallet overview data
        model.addAttribute("wallet", wallet);
        model.addAttribute("currentBalance", wallet.getBalance());
        model.addAttribute("totalDeposited", wallet.getTotalDeposited());
        model.addAttribute("totalSpent", wallet.getTotalSpent());
        
        BigDecimal monthlySpending = walletService.getMonthlySpending(user);
        model.addAttribute("monthlySpending", monthlySpending != null ? monthlySpending : BigDecimal.ZERO);
        
        // Recent transactions (last 10)
        List<WalletTransaction> recentTransactions = walletService.getRecentTransactions(user, 10);
        model.addAttribute("recentTransactions", recentTransactions != null ? recentTransactions : List.of());
        
        // Format numbers for display
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        model.addAttribute("balanceFormatted", currencyFormat.format(wallet.getBalance()) + " đ");
        model.addAttribute("totalDepositedFormatted", currencyFormat.format(wallet.getTotalDeposited() != null ? wallet.getTotalDeposited() : BigDecimal.ZERO) + " đ");
        model.addAttribute("totalSpentFormatted", currencyFormat.format(wallet.getTotalSpent() != null ? wallet.getTotalSpent() : BigDecimal.ZERO) + " đ");
        
        model.addAttribute("content", "owner/wallet/dashboard");
        model.addAttribute("activeMenu", "wallet");
        return "layout/owner-layout";
    }
    
    @GetMapping("/transactions")
    public String transactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model, 
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        Pageable pageable = PageRequest.of(page, size);
        Page<WalletTransaction> transactions = walletService.getTransactionHistory(user, pageable);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentBalance", walletService.getCurrentBalance(user));
        
        model.addAttribute("content", "owner/wallet/transactions");
        model.addAttribute("activeMenu", "wallet");
        return "layout/owner-layout";
    }
    
    @GetMapping("/topup")
    public String showTopupForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        model.addAttribute("currentBalance", walletService.getCurrentBalance(user));
        model.addAttribute("content", "owner/wallet/topup");
        model.addAttribute("activeMenu", "wallet");
        return "layout/owner-layout";
    }
    
    @PostMapping("/topup")
    public String processTopup(
            @RequestParam("amount") BigDecimal amount,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes ra) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login/owner";
        
        try {
            // Validate amount
            if (amount.compareTo(BigDecimal.valueOf(10000)) < 0) {
                ra.addFlashAttribute("errorMessage", "Số tiền nạp tối thiểu là 10,000 VNĐ");
                return "redirect:/owner/wallet/topup";
            }
            
            if (amount.compareTo(BigDecimal.valueOf(50000000)) > 0) {
                ra.addFlashAttribute("errorMessage", "Số tiền nạp tối đa là 50,000,000 VNĐ");
                return "redirect:/owner/wallet/topup";
            }
            
            String ipAddress = request.getRemoteAddr();
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + ((request.getServerPort() == 80 || request.getServerPort() == 443)
                    ? "" : ":" + request.getServerPort());
            
            String returnUrl = baseUrl + "/payment/vnpay/return";
            
            String paymentUrl = walletService.createDepositUrl(user, amount, returnUrl, ipAddress);
            
            return "redirect:" + paymentUrl;
            
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/owner/wallet/topup";
        }
    }
}