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
        // Gói cơ bản
        postingPackageRepository.findByCode("POST_NEW").orElseGet(() -> {
            PostingPackage p = new PostingPackage();
            p.setCode("POST_NEW");
            p.setName("Gói Đăng Tin Cơ Bản");
            p.setDescription("Phù hợp cho chủ trọ mới bắt đầu với ít phòng trọ.");
            p.setPrice(100_000);
            p.setUsageLimit(1);
            p.setActive(true);
            return postingPackageRepository.save(p);
        });

        // Gói tiêu chuẩn
        postingPackageRepository.findByCode("POST_STANDARD").orElseGet(() -> {
            PostingPackage p = new PostingPackage();
            p.setCode("POST_STANDARD");
            p.setName("Gói Tiêu Chuẩn");
            p.setDescription("Dành cho chủ trọ có quy mô vừa với nhiều loại phòng.");
            p.setPrice(250_000);
            p.setUsageLimit(5);
            p.setActive(true);
            return postingPackageRepository.save(p);
        });

        // Gói cao cấp
        postingPackageRepository.findByCode("POST_PREMIUM").orElseGet(() -> {
            PostingPackage p = new PostingPackage();
            p.setCode("POST_PREMIUM");
            p.setName("Gói Cao Cấp");
            p.setDescription("Cho các chủ trọ có quy mô lớn và nhiều địa điểm.");
            p.setPrice(500_000);
            p.setUsageLimit(15);
            p.setActive(true);
            return postingPackageRepository.save(p);
        });

        // Gói doanh nghiệp
        postingPackageRepository.findByCode("POST_ENTERPRISE").orElseGet(() -> {
            PostingPackage p = new PostingPackage();
            p.setCode("POST_ENTERPRISE");
            p.setName("Gói Doanh Nghiệp");
            p.setDescription("Giải pháp toàn diện cho các công ty quản lý bất động sản.");
            p.setPrice(1_000_000);
            p.setUsageLimit(50);
            p.setActive(true);
            return postingPackageRepository.save(p);
        });
    }
}