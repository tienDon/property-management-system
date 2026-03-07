package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.PostPackage;
import com.pms.propertymanagement.repository.PostPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostPackageInitializer {

    private final PostPackageRepository postPackageRepository;

    public void init() {
        // BASIC - 15 days
        postPackageRepository.findByCode("BASIC_15").orElseGet(() -> {
            PostPackage pack = new PostPackage();
            pack.setCode("BASIC_15");
            pack.setName("Gói Cơ bản 15 ngày");
            pack.setDescription("Đăng tin cho thuê nhà trọ trong 15 ngày");
            pack.setDurationDays(15);
            pack.setPrice(20_000);
            pack.setFreeBoosts(0);
            pack.setHasVipBadge(false);
            pack.setHasSearchPriority(false);
            pack.setSortOrder(1);
            pack.setActive(true);
            return postPackageRepository.save(pack);
        });

        // STANDARD - 30 days
        postPackageRepository.findByCode("STANDARD_30").orElseGet(() -> {
            PostPackage pack = new PostPackage();
            pack.setCode("STANDARD_30");
            pack.setName("Gói Tiêu chuẩn 30 ngày");
            pack.setDescription("Đăng tin cho thuê nhà trọ trong 30 ngày với 1 lần đẩy tin miễn phí");
            pack.setDurationDays(30);
            pack.setPrice(35_000);
            pack.setFreeBoosts(1);
            pack.setHasVipBadge(false);
            pack.setHasSearchPriority(false);
            pack.setSortOrder(2);
            pack.setActive(true);
            return postPackageRepository.save(pack);
        });

        // VIP - 30 days
        postPackageRepository.findByCode("VIP_30").orElseGet(() -> {
            PostPackage pack = new PostPackage();
            pack.setCode("VIP_30");
            pack.setName("Gói VIP 30 ngày");
            pack.setDescription("Đăng tin VIP với huy hiệu đặc biệt và 3 lần đẩy tin miễn phí");
            pack.setDurationDays(30);
            pack.setPrice(75_000);
            pack.setFreeBoosts(3);
            pack.setHasVipBadge(true);
            pack.setHasSearchPriority(true);
            pack.setSortOrder(3);
            pack.setActive(true);
            return postPackageRepository.save(pack);
        });

        // PREMIUM - 60 days
        postPackageRepository.findByCode("PREMIUM_60").orElseGet(() -> {
            PostPackage pack = new PostPackage();
            pack.setCode("PREMIUM_60");
            pack.setName("Gói Cao cấp 60 ngày");
            pack.setDescription("Đăng tin cao cấp trong 60 ngày với nhiều ưu đãi");
            pack.setDurationDays(60);
            pack.setPrice(120_000);
            pack.setFreeBoosts(5);
            pack.setHasVipBadge(true);
            pack.setHasSearchPriority(true);
            pack.setSortOrder(4);
            pack.setActive(true);
            return postPackageRepository.save(pack);
        });
    }
}