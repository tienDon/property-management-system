package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.PostingPackage;
import com.pms.propertymanagement.repository.PostingPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostingPackageInitializer {

    private final PostingPackageRepository postingPackageRepository;

    public void init() {
        upsert("POST_NEW",    "Gói 7 ngày",  "Gia hạn bài đăng thêm 7 ngày.",  29_000,  7, 0, false, false, false);
        upsert("POST_STANDARD","Gói 14 ngày","Gia hạn bài đăng thêm 14 ngày.", 49_000, 14, 0, false, false, true);
        upsert("POST_PREMIUM", "Gói 30 ngày","Gia hạn bài đăng thêm 30 ngày.", 89_000, 30, 0, false, false, false);

        // Deactivate legacy enterprise package
        postingPackageRepository.findByCode("POST_ENTERPRISE").ifPresent(p -> {
            p.setActive(false);
            postingPackageRepository.save(p);
        });
    }

    private void upsert(String code, String name, String description,
                        int price, int durationDays, int freeBoosts,
                        boolean hasVipBadge, boolean hasSearchPriority, boolean recommended) {
        PostingPackage p = postingPackageRepository.findByCode(code).orElseGet(PostingPackage::new);
        p.setCode(code);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setUsageLimit(durationDays);
        p.setFreeBoosts(freeBoosts);
        p.setHasVipBadge(hasVipBadge);
        p.setHasSearchPriority(hasSearchPriority);
        p.setRecommended(recommended);
        p.setActive(true);
        postingPackageRepository.save(p);
    }
}