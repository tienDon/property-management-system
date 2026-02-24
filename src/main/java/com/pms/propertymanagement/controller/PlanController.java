package com.pms.propertymanagement.controller;

import com.pms.propertymanagement.dto.ManagementPlanDTO;
import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.entity.Subscription;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.entity.UserWallet;
import com.pms.propertymanagement.service.SubscriptionManagementService;
import com.pms.propertymanagement.service.PropertyPolicyService;
import com.pms.propertymanagement.service.ManagementPlanService;
import com.pms.propertymanagement.service.PostPackageService;
import com.pms.propertymanagement.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * NEW ARCHITECTURE: Controller quản lý Subscription-based Management Plans và Post Packages
 * Integrates với SubscriptionManagementService và PropertyPolicyService
 */
@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
@Slf4j
public class PlanController {
    
    // === NEW ARCHITECTURE SERVICES ===
    private final SubscriptionManagementService subscriptionManagementService;
    private final PropertyPolicyService propertyPolicyService;
    private final ManagementPlanService managementPlanService;
    private final PostPackageService postPackageService;
    private final WalletService walletService;
    
    /**
     * === MANAGEMENT PLANS ===
     */
    
    /**
     * NEW ARCHITECTURE: Hiển thị trang Management Plans với subscription model
     */
    @GetMapping("/management-plans")
    public String managementPlansPage(Model model, HttpSession session) {
        try {
            User owner = getCurrentOwner(session);
            
            // Get current active management subscription
            Subscription currentSubscription = subscriptionManagementService.getActiveManagementSubscription(owner.getId());
            
            // Get all available management plans
            List<ManagementPlanDTO> availablePlans = managementPlanService.getAllPlans();
            
            // Get property usage statistics
            PropertyPolicyService.PropertyCounts propertyCounts = propertyPolicyService.getPropertyCounts(owner.getId());
            
            // Determine current plan limits and usage
            Integer currentPropertyLimit = null;
            String currentPlanName = "Không có gói";
            boolean hasActiveSubscription = false;
            
            if (currentSubscription != null) {
                var currentPlan = managementPlanService.getById(currentSubscription.getManagementPlanId());
                currentPropertyLimit = currentPlan.getMaxProperties();
                currentPlanName = currentPlan.getName();
                hasActiveSubscription = true;
            }
            
            // Calculate available slots
            int availableSlots = hasActiveSubscription ? 
                Math.max(0, currentPropertyLimit - propertyCounts.active()) : 0;
            
            boolean canCreateProperty = propertyPolicyService.canCreateProperty(owner.getId());
            
            model.addAttribute("currentSubscription", currentSubscription);
            model.addAttribute("currentPlanName", currentPlanName);
            model.addAttribute("availablePlans", availablePlans);
            model.addAttribute("propertyCounts", propertyCounts);
            model.addAttribute("currentPropertyLimit", currentPropertyLimit);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("canCreateProperty", canCreateProperty);
            model.addAttribute("hasActiveSubscription", hasActiveSubscription);
            
            model.addAttribute("content", "owner/package/management-plans");
            model.addAttribute("activeMenu", "management-plans");
            return "layout/owner-layout";
            
        } catch (Exception e) {
            log.error("Error loading management plans page for user", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải trang: " + e.getMessage());
            model.addAttribute("availablePlans", java.util.List.of());
            model.addAttribute("propertyCounts", new PropertyPolicyService.PropertyCounts(0, 0, 0, 0));
            model.addAttribute("currentPropertyLimit", 0);
            model.addAttribute("availableSlots", 0);
            model.addAttribute("canCreateProperty", false);
            model.addAttribute("hasActiveSubscription", false);
            model.addAttribute("currentPlanName", "N/A");
            model.addAttribute("content", "owner/package/management-plans");
            model.addAttribute("activeMenu", "management-plans");
            return "layout/owner-layout";
        }
    }
    
    /**
     * NEW ARCHITECTURE: Tạo hoặc nâng cấp Management Plan subscription
     */
    @PostMapping("/management-plans/subscribe/{planId}")
    public String subscribeToManagementPlan(@PathVariable Long planId,
                                           @RequestParam(defaultValue = "30") int durationDays,
                                           HttpSession session,
                                           RedirectAttributes redirectAttributes) {
        try {
            User owner = getCurrentOwner(session);
            
            // Create or upgrade management subscription
            subscriptionManagementService.createManagementSubscription(
                owner.getId(), planId, durationDays);

            // Force wallet cache refresh so header shows updated balance immediately
            session.setAttribute("lastWalletUpdate", 0L);

            var plan = managementPlanService.getById(planId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Đăng ký thành công gói %s. Hạn sử dụng: %d ngày", 
                    plan.getName(), durationDays));
            
            log.info("User {} subscribed to management plan {}", owner.getId(), planId);
            
        } catch (IllegalStateException e) {
            log.error("Error subscribing to management plan {} for user", planId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error subscribing to management plan {} for user", planId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Có lỗi xảy ra khi đăng ký gói. Vui lòng thử lại.");
        }
        
        return "redirect:/owner/management-plans";
    }
    
    /**
     * NEW ARCHITECTURE: Upgrade management plan (atomic transition)
     */
    @PostMapping("/management-plans/upgrade/{newPlanId}")
    public String upgradeManagementPlan(@PathVariable Long newPlanId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        try {
            User owner = getCurrentOwner(session);
            
            // Use atomic upgrade method
            subscriptionManagementService.upgradeManagementPlan(owner.getId(), newPlanId);

            // Force wallet cache refresh so header shows updated balance immediately
            session.setAttribute("lastWalletUpdate", 0L);

            var newPlan = managementPlanService.getById(newPlanId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Chuyển đổi thành công sang gói %s", newPlan.getName()));
                
            log.info("User {} upgraded to management plan {}", owner.getId(), newPlanId);
            
        } catch (IllegalStateException e) {
            log.error("Error upgrading management plan to {} for user", newPlanId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error upgrading management plan to {} for user", newPlanId, e);
            redirectAttributes.addFlashAttribute("errorMessage",
                "Có lỗi xảy ra khi chuyển đổi gói. Vui lòng thử lại.");
        }
        
        return "redirect:/owner/management-plans";
    }
    
    /**
     * === POST PACKAGES (NEW ARCHITECTURE) ===
     */
    
    /**
     * NEW ARCHITECTURE: Hiển thị trang Post Packages với subscription model
     */
    @GetMapping("/post-packages")
    public String postPackagesPage() {
        // Post packages replaced by management plans
        return "redirect:/owner/management-plans";
    }

    @SuppressWarnings("unused")
    private String _legacyPostPackagesPage(Model model, HttpSession session) {
        try {
            User owner = getCurrentOwner(session);

            // Get available post packages
            List<PostPackageDTO> postPackages = postPackageService.getAllPackages();
            
            // Get user's current post subscriptions
            List<Subscription> activePostSubscriptions = subscriptionManagementService.getUserSubscriptions(owner.getId())
                .stream()
                .filter(s -> s.isPost() && s.isActive())
                .toList();
            
            // Get subscription counts
            var subscriptionCounts = subscriptionManagementService.getSubscriptionCounts(owner.getId());
            
            model.addAttribute("postPackages", postPackages);
            model.addAttribute("activePostSubscriptions", activePostSubscriptions);
            model.addAttribute("subscriptionCounts", subscriptionCounts);
            
            model.addAttribute("content", "owner/package/list");
            return "layout/owner-layout";
            
        } catch (Exception e) {
            log.error("Error loading post packages page for user", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi tải trang: " + e.getMessage());
            model.addAttribute("content", "owner/package/list");
            return "layout/owner-layout";
        }
    }
    
    /**
     * NEW ARCHITECTURE: Tạo POST subscription cho property
     */
    @PostMapping("/post-packages/subscribe")
    public String subscribeToPostPackage(@RequestParam Long postPackageId,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        try {
            User owner = getCurrentOwner(session);
            
            // Create post subscription (deducts wallet balance)
            subscriptionManagementService.createPostSubscription(
                owner.getId(), postPackageId);
            
            var postPackage = postPackageService.getById(postPackageId);
            
            // CRITICAL: Refresh wallet in session after transaction
            UserWallet updatedWallet = walletService.getOrCreateWallet(owner);
            session.setAttribute("userWallet", updatedWallet);
            session.setAttribute("lastWalletUpdate", System.currentTimeMillis());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                String.format("Đăng ký thành công gói đăng tin %s. Số dư ví đã được trừ %,d VND", 
                    postPackage.getName(), postPackage.getPrice()));
                
            log.info("User {} subscribed to post package {}", owner.getId(), postPackageId);
            
        } catch (IllegalStateException e) {
            // Wallet balance insufficient
            log.error("Insufficient balance for user subscribing to post package {}", postPackageId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Error subscribing to post package {} for user", postPackageId, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi đăng ký gói: " + e.getMessage());
        }
        
        return "redirect:/owner/post-packages";
    }
    
    /**
     * NEW ARCHITECTURE: Hủy subscription (manual cancellation)
     */
    @PostMapping("/subscriptions/cancel/{subscriptionId}")
    public String cancelSubscription(@PathVariable Long subscriptionId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            User owner = getCurrentOwner(session);
            
            subscriptionManagementService.cancelSubscription(subscriptionId, owner.getId());
            
            redirectAttributes.addFlashAttribute("successMessage", "Hủy đăng ký thành công");
            
            log.info("User {} cancelled subscription {}", owner.getId(), subscriptionId);
            
        } catch (Exception e) {
            log.error("Error cancelling subscription {} for user", subscriptionId, e);
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Có lỗi xảy ra khi hủy đăng ký: " + e.getMessage());
        }
        
        return "redirect:/owner/management-plans";
    }
    
    /**
     * API endpoint to get subscription status
     */
    @GetMapping("/api/subscription-status")
    @ResponseBody
    public ResponseEntity<?> getSubscriptionStatus(HttpSession session) {
        try {
            User owner = getCurrentOwner(session);
            
            var subscriptionCounts = subscriptionManagementService.getSubscriptionCounts(owner.getId());
            var propertyCounts = propertyPolicyService.getPropertyCounts(owner.getId());
            
            return ResponseEntity.ok(Map.of(
                "subscriptionCounts", subscriptionCounts,
                "propertyCounts", propertyCounts
            ));
            
        } catch (Exception e) {
            log.error("Error getting subscription status", e);
            return ResponseEntity.internalServerError().body(
                Map.of("error", "Có lỗi xảy ra"));
        }
    }
    
    /**
     * === HELPER METHODS ===
     */
    
    private User getCurrentOwner(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("User not logged in");
        }
        return user;
    }
}