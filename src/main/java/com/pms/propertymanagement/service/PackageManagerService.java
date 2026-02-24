package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.ManagementPlanDTO;
import com.pms.propertymanagement.dto.OwnerSubscriptionDTO;
import com.pms.propertymanagement.dto.PostPackageDTO;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.User;

import java.util.List;

/**
 * Service tổng hợp để quản lý cả Management Plan (gói quản lý định kỳ) 
 * và Post Package (gói đăng tin theo thời gian)
 * 
 * Concept: 1 Property = 1 Post với upgrade path tự nhiên:
 * - Muốn nhiều property hơn → upgrade Management Plan
 * - Muốn đăng tin lâu hơn/tốt hơn → mua Post Package
 */
public interface PackageManagerService {

    // ========== MANAGEMENT PLAN OPERATIONS ==========
    
    /**
     * Lấy tất cả gói quản lý có thể upgrade
     */
    List<ManagementPlanDTO> getAvailableManagementPlans();
    
    /**
     * Lấy thông tin subscription hiện tại của owner
     */
    OwnerSubscriptionDTO getOwnerSubscription(User owner);
    
    /**
     * Upgrade/Downgrade gói quản lý
     */
    OwnerSubscriptionDTO upgradeManagementPlan(User owner, String planCode);
    
    /**
     * Kiểm tra owner có thể tạo property mới không
     */
    boolean canCreateProperty(User owner);
    
    /**
     * Lấy số slot còn lại
     */
    int getAvailablePropertySlots(User owner);
    
    // ========== POST PACKAGE OPERATIONS ==========
    
    /**
     * Lấy tất cả gói đăng tin
     */
    List<PostPackageDTO> getAvailablePostPackages();
    
    /**
     * Tạo post mới cho property với package đã chọn
     */
    void createPropertyPost(Property property, String postPackageCode);
    
    /**
     * Gia hạn post hiện tại với package mới
     */
    void renewPropertyPost(Property property, String postPackageCode);
    
    /**
     * Kiểm tra property có post đang active không
     */
    boolean hasActivePost(Property property);
    
    /**
     * Lấy thông tin post hiện tại của property
     */
    String getPropertyPostStatus(Property property);
    
    // ========== INTEGRATED OPERATIONS ==========
    
    /**
     * Workflow tạo property mới:
     * 1. Kiểm tra subscription có đủ slot không
     * 2. Tạo property
     * 3. Tạo post với package cơ bản
     */
    void createPropertyWithPost(Property property, String postPackageCode, User owner);
    
    /**
     * Dashboard summary cho owner
     */
    PackageSummaryDTO getOwnerPackageSummary(User owner);
    
    /**
     * Class DTO cho summary
     */
    public static class PackageSummaryDTO {
        public OwnerSubscriptionDTO subscription;
        public List<PropertyPostSummaryDTO> activePosts;
        public List<PropertyPostSummaryDTO> expiringPosts;
        
        public static class PropertyPostSummaryDTO {
            public Long propertyId;
            public String propertyName;
            public String postStatus;
            public String postPackageName;
            public int daysLeft;
            public boolean canRenew;
        }
    }
}