package com.pms.propertymanagement.service;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.entity.OwnerSubscription;
import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.entity.PropertyPost;
import com.pms.propertymanagement.entity.PropertySlot;
import com.pms.propertymanagement.enums.PostStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý Plan System theo kiến trúc mới
 * 
 * Kiến trúc:
 * 1. Management Plan = quản lý nội bộ (recurring)
 * 2. plan_slot = kiểm soát số property
 * 3. Post Package = marketplace (transaction-based)
 */
@Service
@Slf4j
public class PlanService {

    // Mock repositories - thực tế cần inject các repository
    
    /**
     * === MANAGEMENT PLAN OPERATIONS ===
     */
    
    /**
     * Lấy gói Management hiện tại của owner
     */
    public OwnerSubscription getCurrentSubscription(Long ownerId) {
        // Logic lấy subscription hiện tại
        // SELECT * FROM owner_subscriptions 
        // WHERE owner_id = ? AND status = 'ACTIVE'
        return null; // Mock
    }
    
    /**
     * Nâng cấp Management Plan
     */
    @Transactional
    public boolean upgradeManagementPlan(Long ownerId, String newPlanCode) {
        try {
            OwnerSubscription currentSub = getCurrentSubscription(ownerId);
            ManagementPlan newPlan = getManagementPlanByCode(newPlanCode);
            
            if (newPlan == null) {
                log.error("Plan không tồn tại: {}", newPlanCode);
                return false;
            }
            
            // Validate upgrade path
            if (!canUpgrade(currentSub.getManagementPlan(), newPlan)) {
                log.error("Không thể nâng cấp từ {} lên {}", 
                    currentSub.getManagementPlan().getCode(), newPlanCode);
                return false;
            }
            
            // Check wallet balance for paid plans
            if (!newPlan.isFree()) {
                BigDecimal walletBalance = getWalletBalance(ownerId);
                if (walletBalance.compareTo(BigDecimal.valueOf(newPlan.getMonthlyPrice())) < 0) {
                    log.error("Số dư không đủ. Cần: {}, Có: {}", 
                        newPlan.getMonthlyPrice(), walletBalance);
                    return false;
                }
            }
            
            // Execute upgrade
            return executeUpgrade(ownerId, currentSub, newPlan);
            
        } catch (Exception e) {
            log.error("Lỗi nâng cấp plan cho owner {}: {}", ownerId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Hạ cấp Management Plan (downgrade)
     */
    @Transactional
    public boolean downgradeManagementPlan(Long ownerId, String newPlanCode, 
                                          List<Long> propertiesToKeepSlot) {
        try {
            OwnerSubscription currentSub = getCurrentSubscription(ownerId);
            ManagementPlan newPlan = getManagementPlanByCode(newPlanCode);
            
            // Validate downgrade
            if (!canDowngrade(currentSub.getManagementPlan(), newPlan)) {
                return false;
            }
            
            // Handle slot management
            return executeDowngrade(ownerId, currentSub, newPlan, propertiesToKeepSlot);
            
        } catch (Exception e) {
            log.error("Lỗi hạ cấp plan cho owner {}: {}", ownerId, e.getMessage());
            return false;
        }
    }
    
    /**
     * === PROPERTY SLOT OPERATIONS ===
     */
    
    /**
     * Tạo property mới (kiểm tra slot)
     */
    @Transactional
    public boolean createPropertyWithSlot(Long ownerId, PropertyCreateDTO propertyData) {
        try {
            OwnerSubscription subscription = getCurrentSubscription(ownerId);
            
            // Check slot availability
            if (!subscription.canCreateProperty()) {
                log.warn("Owner {} đã hết slot property. Used: {}, Max: {}", 
                    ownerId, subscription.getUsedPropertySlots(), 
                    subscription.getManagementPlan().getMaxProperties());
                return false;
            }
            
            // Create property
            Long propertyId = createProperty(propertyData);
            
            // Create slot
            PropertySlot slot = new PropertySlot();
            slot.setPropertyId(propertyId);
            slot.setOwnerId(ownerId);
            slot.setActive(true);
            savePropertySlot(slot);
            
            // Update used slots
            subscription.setUsedPropertySlots(subscription.getUsedPropertySlots() + 1);
            saveOwnerSubscription(subscription);
            
            log.info("Tạo property {} với slot cho owner {}", propertyId, ownerId);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi tạo property cho owner {}: {}", ownerId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Kích hoạt/vô hiệu hóa property slot
     */
    @Transactional
    public boolean togglePropertySlot(Long propertyId, boolean active, String reason) {
        try {
            PropertySlot slot = getPropertySlotByPropertyId(propertyId);
            if (slot == null) return false;
            
            slot.setActive(active);
            if (!active) {
                slot.setDeactivationReason(reason);
                // Hủy post đang chạy nếu có
                cancelActivePost(propertyId, "Property slot deactivated");
            }
            
            savePropertySlot(slot);
            
            log.info("Toggled property {} slot to {}", propertyId, active);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi toggle slot property {}: {}", propertyId, e.getMessage());
            return false;
        }
    }
    
    /**
     * === POST PACKAGE OPERATIONS ===
     */
    
    /**
     * Mua gói đăng tin
     */
    @Transactional
    public boolean purchasePostPackage(Long ownerId, Long propertyId, String packageCode) {
        try {
            // Validate conditions
            if (!canCreatePost(ownerId, propertyId)) {
                return false;
            }
            
            PostingPackage postPackage = getPostPackageByCode(packageCode);
            if (postPackage == null) return false;
            
            // Check wallet balance
            BigDecimal walletBalance = getWalletBalance(ownerId);
            if (walletBalance.compareTo(BigDecimal.valueOf(postPackage.getPrice())) < 0) {
                log.error("Số dư không đủ để mua gói post. Cần: {}, Có: {}", 
                    postPackage.getPrice(), walletBalance);
                return false;
            }
            
            // Deduct wallet
            if (!deductWallet(ownerId, BigDecimal.valueOf(postPackage.getPrice()), 
                             "Mua gói đăng tin " + postPackage.getName())) {
                return false;
            }
            
            // Create post with NEW ARCHITECTURE duration mapping
            int durationDays = calculateDuration(postPackage.getCode());
            
            PropertyPost post = new PropertyPost();
            post.setPropertyId(propertyId);
            post.setOwnerId(ownerId);
            post.setPostPackage(postPackage);
            post.setStartDate(LocalDateTime.now());
            post.setExpiryDate(LocalDateTime.now().plusDays(durationDays));
            post.setStatus(PostStatus.ACTIVE);
            
            savePropertyPost(post);
            
            log.info("Mua gói post {} cho property {} của owner {}", 
                packageCode, propertyId, ownerId);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi mua gói post: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gia hạn tự động post
     */
    @Transactional
    public void processAutoRenewals() {
        try {
            List<PropertyPost> eligiblePosts = getEligibleForRenewal();
            
            for (PropertyPost post : eligiblePosts) {
                try {
                    // Check conditions
                    if (!canRenewPost(post)) continue;
                    
                    // Check wallet balance
                    BigDecimal walletBalance = getWalletBalance(post.getOwnerId());
                    if (walletBalance.compareTo(BigDecimal.valueOf(post.getPostPackage().getPrice())) < 0) {
                        log.warn("Không đủ tiền gia hạn post {} của owner {}", 
                            post.getId(), post.getOwnerId());
                        continue;
                    }
                    
                    // Process renewal
                    renewPost(post);
                    
                } catch (Exception e) {
                    log.error("Lỗi gia hạn post {}: {}", post.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("Lỗi xử lý gia hạn tự động: {}", e.getMessage());
        }
    }
    
    /**
     * === VALIDATION METHODS ===
     */
    
    /**
     * Kiểm tra có thể tạo post không
     */
    private boolean canCreatePost(Long ownerId, Long propertyId) {
        // 1. Check property slot is active
        PropertySlot slot = getPropertySlotByPropertyId(propertyId);
        if (slot == null || !slot.isActive()) {
            log.warn("Property {} không có slot active", propertyId);
            return false;
        }
        
        // 2. Check property belongs to owner
        Property property = getPropertyById(propertyId);
        if (property == null || !property.getOwnerId().equals(ownerId)) {
            log.warn("Property {} không thuộc owner {}", propertyId, ownerId);
            return false;
        }
        
        // 3. Check no existing active post
        PropertyPost existingPost = getActivePostByProperty(propertyId);
        if (existingPost != null) {
            log.warn("Property {} đã có post đang chạy", propertyId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Kiểm tra có thể gia hạn post không
     */
    private boolean canRenewPost(PropertyPost post) {
        // Check property still has active slot
        PropertySlot slot = getPropertySlotByPropertyId(post.getPropertyId());
        return slot != null && slot.isActive();
    }
    
    /**
     * === HELPER METHODS ===
     */
    
    private boolean canUpgrade(ManagementPlan current, ManagementPlan target) {
        // Any plan can upgrade to a higher tier
        return target.getSortOrder() > current.getSortOrder();
    }
    
    private boolean canDowngrade(ManagementPlan current, ManagementPlan target) {
        // Any plan can downgrade to a lower tier
        return target.getSortOrder() < current.getSortOrder();
    }
    
    @Transactional
    private boolean executeUpgrade(Long ownerId, OwnerSubscription currentSub, 
                                 ManagementPlan newPlan) {
        try {
            // Deduct payment (if not free)
            if (!newPlan.isFree()) {
                if (!deductWallet(ownerId, BigDecimal.valueOf(newPlan.getMonthlyPrice()), 
                                "Nâng cấp " + newPlan.getName())) {
                    return false;
                }
            }
            
            // Update subscription
            currentSub.setManagementPlan(newPlan);
            currentSub.setNextBillingDate(LocalDateTime.now().plusMonths(1));
            saveOwnerSubscription(currentSub);
            
            log.info("Nâng cấp thành công owner {} lên {}", ownerId, newPlan.getCode());
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi execute upgrade: {}", e.getMessage());
            return false;
        }
    }
    
    @Transactional
    private boolean executeDowngrade(Long ownerId, OwnerSubscription currentSub, 
                                   ManagementPlan newPlan, List<Long> propertiesToKeepSlot) {
        try {
            // Handle slot redistribution
            int newMaxSlots = newPlan.isUnlimitedProperties() ? 
                Integer.MAX_VALUE : newPlan.getMaxProperties();
            
            List<PropertySlot> ownerSlots = getPropertySlotsByOwner(ownerId);
            
            // Deactivate excess slots
            int keptSlots = 0;
            for (PropertySlot slot : ownerSlots) {
                if (propertiesToKeepSlot.contains(slot.getPropertyId()) && 
                    keptSlots < newMaxSlots) {
                    keptSlots++;
                } else {
                    // Deactivate slot
                    togglePropertySlot(slot.getPropertyId(), false, 
                        "Downgrade to " + newPlan.getCode());
                }
            }
            
            // Update subscription
            currentSub.setManagementPlan(newPlan);
            currentSub.setUsedPropertySlots(keptSlots);
            currentSub.setNextBillingDate(LocalDateTime.now().plusMonths(1));
            saveOwnerSubscription(currentSub);
            
            log.info("Hạ cấp thành công owner {} xuống {}, giữ {} slots", 
                ownerId, newPlan.getCode(), keptSlots);
            return true;
            
        } catch (Exception e) {
            log.error("Lỗi execute downgrade: {}", e.getMessage());
            return false;
        }
    }
    
    private void renewPost(PropertyPost post) {
        // Deduct wallet
        deductWallet(post.getOwnerId(), BigDecimal.valueOf(post.getPostPackage().getPrice()), 
            "Gia hạn tin đăng " + post.getPostPackage().getName());
        
        // NEW ARCHITECTURE: Calculate duration based on package code
        int durationDays = calculateDuration(post.getPostPackage().getCode());
        
        // Extend expiry date
        post.setExpiryDate(post.getExpiryDate().plusDays(durationDays));
        post.setLastRenewalDate(LocalDateTime.now());
        
        savePropertyPost(post);
        
        log.info("Gia hạn post {} thành công", post.getId());
    }
    
    // NEW ARCHITECTURE: Calculate duration based on package code
    private int calculateDuration(String code) {
        return switch (code) {
            case "POST_NEW" -> 15;       // 100k for 15 days
            case "POST_STANDARD" -> 30;  // 250k for 30 days
            case "POST_PREMIUM" -> 60;   // 500k for 60 days
            case "POST_ENTERPRISE" -> 90; // 1M for 90 days
            default -> 15;
        };
    }
    
    // === MOCK DATA ACCESS METHODS ===
    // Thực tế sẽ inject các repository
    
    private ManagementPlan getManagementPlanByCode(String code) { return null; }
    private PostingPackage getPostPackageByCode(String code) { return null; }
    private PropertySlot getPropertySlotByPropertyId(Long propertyId) { return null; }
    private List<PropertySlot> getPropertySlotsByOwner(Long ownerId) { return null; }
    private PropertyPost getActivePostByProperty(Long propertyId) { return null; }
    private List<PropertyPost> getEligibleForRenewal() { return null; }
    private Property getPropertyById(Long propertyId) { return null; }
    private BigDecimal getWalletBalance(Long ownerId) { return BigDecimal.ZERO; }
    private boolean deductWallet(Long ownerId, BigDecimal amount, String description) { return true; }
    private Long createProperty(PropertyCreateDTO data) { return 1L; }
    private void savePropertySlot(PropertySlot slot) {}
    private void saveOwnerSubscription(OwnerSubscription subscription) {}
    private void savePropertyPost(PropertyPost post) {}
    private void cancelActivePost(Long propertyId, String reason) {}
    
    // === DTOs ===
    public static class PropertyCreateDTO {
        private String name;
        private String address;
        // ... other fields
    }
    
    // Mock classes - thực tế cần implement đầy đủ
    static class Property {
        private Long id;
        private Long ownerId;
        private String name;
        private String address;
        
        public Long getOwnerId() { return ownerId; }
    }
}