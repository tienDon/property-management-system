package com.pms.propertymanagement.interceptor;

import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.service.FreeTrialService;
import com.pms.propertymanagement.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class WalletInterceptor implements HandlerInterceptor {
    
    private final WalletService walletService;
    private final FreeTrialService freeTrialService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        // Only load wallet for authenticated users accessing owner pages
        if (user != null && request.getRequestURI().startsWith("/owner")) {
            UserWallet userWallet = (UserWallet) session.getAttribute("userWallet");
            
            // Load wallet if not already in session or if it's been more than 5 minutes
            Long lastWalletUpdate = (Long) session.getAttribute("lastWalletUpdate");
            long currentTime = System.currentTimeMillis();
            
            if (userWallet == null || 
                lastWalletUpdate == null || 
                (currentTime - lastWalletUpdate) > 300000) { // 5 minutes
                
                userWallet = walletService.getOrCreateWallet(user);
                session.setAttribute("userWallet", userWallet);
                session.setAttribute("lastWalletUpdate", currentTime);
            }
            
            // AUTO-CREATE FREE TRIAL: Check if new user needs FREE subscription
            Boolean freeTrialChecked = (Boolean) session.getAttribute("freeTrialChecked");
            if (freeTrialChecked == null || !freeTrialChecked) {
                freeTrialService.createFreeTrialIfEligible(user);
                session.setAttribute("freeTrialChecked", true);
            }
        }
        
        return true;
    }
}