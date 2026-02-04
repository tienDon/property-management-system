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
        postingPackageRepository.findByCode("POST_NEW").orElseGet(() -> {
            PostingPackage p = new PostingPackage();
            p.setCode("POST_NEW");
            p.setName("Gói Đăng Tin Nhà Trọ Mới");
            p.setDescription("Sử dụng 1 lần để đăng 1 tin mới.");
            p.setPrice(500_000);
            p.setUsageLimit(1);
            p.setActive(true);
            return postingPackageRepository.save(p);
        });
    }
}