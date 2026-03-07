package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.ManagementPlan;
import com.pms.propertymanagement.repository.ManagementPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ManagementPlanInitializer {

    private final ManagementPlanRepository managementPlanRepository;

    public void init() {
        // Deactivate old plans (PRO, BUSINESS) if they exist
        managementPlanRepository.findByCode("PRO").ifPresent(p -> { p.setActive(false); managementPlanRepository.save(p); });
        managementPlanRepository.findByCode("BUSINESS").ifPresent(p -> { p.setActive(false); managementPlanRepository.save(p); });

        // FREE Plan — 1 nhà trọ, 1 bài đăng, 3 phòng, 7 ngày/bài
        ManagementPlan free = managementPlanRepository.findByCode("FREE").orElse(new ManagementPlan());
        free.setCode("FREE"); free.setName("Free");
        free.setDescription("Dùng thử miễn phí cho người mới bắt đầu");
        free.setMonthlyPrice(0); free.setMaxProperties(1); free.setMaxPosts(1);
        free.setMaxRoomsPerProperty(3); free.setPostDurationDays(7);
        free.setHasContracts(false); free.setHasInvoices(false); free.setHasAdvancedReports(false);
        free.setHasAutoReminders(false); free.setHasStaffManagement(false); free.setHasApiAccess(false); free.setHasExcelExport(false);
        free.setSortOrder(1); free.setActive(true);
        managementPlanRepository.save(free);

        // PREMIUM Plan — 3 nhà trọ, 3 bài đăng, 15 phòng, 14 ngày/bài
        ManagementPlan premium = managementPlanRepository.findByCode("PREMIUM").orElse(new ManagementPlan());
        premium.setCode("PREMIUM"); premium.setName("Premium");
        premium.setDescription("Phù hợp cho chủ trọ vừa và nhỏ có nhiều nhà trọ");
        premium.setMonthlyPrice(99_000); premium.setMaxProperties(3); premium.setMaxPosts(3);
        premium.setMaxRoomsPerProperty(15); premium.setPostDurationDays(14);
        premium.setHasContracts(true); premium.setHasInvoices(true); premium.setHasAdvancedReports(false);
        premium.setHasAutoReminders(false); premium.setHasStaffManagement(false); premium.setHasApiAccess(false); premium.setHasExcelExport(false);
        premium.setSortOrder(2); premium.setActive(true);
        managementPlanRepository.save(premium);

        // VIP Plan — 5 nhà trọ, 5 bài đăng, 30 phòng, 20 ngày/bài
        ManagementPlan vip = managementPlanRepository.findByCode("VIP").orElse(new ManagementPlan());
        vip.setCode("VIP"); vip.setName("VIP");
        vip.setDescription("Dành cho chủ trọ lớn với nhiều tài sản");
        vip.setMonthlyPrice(199_000); vip.setMaxProperties(5); vip.setMaxPosts(5);
        vip.setMaxRoomsPerProperty(30); vip.setPostDurationDays(20);
        vip.setHasContracts(true); vip.setHasInvoices(true); vip.setHasAdvancedReports(true);
        vip.setHasAutoReminders(true); vip.setHasStaffManagement(false); vip.setHasApiAccess(false); vip.setHasExcelExport(true);
        vip.setSortOrder(3); vip.setActive(true);
        managementPlanRepository.save(vip);

        // ENTERPRISE Plan — không giới hạn, 30 ngày/bài tự động
        ManagementPlan enterprise = managementPlanRepository.findByCode("ENTERPRISE").orElse(new ManagementPlan());
        enterprise.setCode("ENTERPRISE"); enterprise.setName("Doanh nghiệp");
        enterprise.setDescription("Doanh nghiệp và chuỗi nhà trọ lớn, không giới hạn");
        enterprise.setMonthlyPrice(399_000); enterprise.setMaxProperties(-1); enterprise.setMaxPosts(-1);
        enterprise.setMaxRoomsPerProperty(-1); enterprise.setPostDurationDays(30);
        enterprise.setHasContracts(true); enterprise.setHasInvoices(true); enterprise.setHasAdvancedReports(true);
        enterprise.setHasAutoReminders(true); enterprise.setHasStaffManagement(true); enterprise.setHasApiAccess(true); enterprise.setHasExcelExport(true);
        enterprise.setSortOrder(4); enterprise.setActive(true);
        managementPlanRepository.save(enterprise);
    }
}