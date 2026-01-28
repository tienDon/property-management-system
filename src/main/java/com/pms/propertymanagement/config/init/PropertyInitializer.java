package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.CategoryRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.repository.UserRepository;
import com.pms.propertymanagement.repository.WardRepository;
import com.pms.propertymanagement.utils.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PropertyInitializer {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final CategoryRepository categoryRepository;

    public void init() {
        if (!propertyRepository.findAll().isEmpty()) return;

        User owner = userRepository.findByUsername("owner1").orElse(null);
        Ward ward = wardRepository.findById("27610").orElse(null);
        Category category = categoryRepository.findByName("Nhà trọ");



        if (owner == null || ward == null || category == null) return;

        Property p = new Property();
        p.setName("Nhà trọ 341");
        p.setTitle("Phòng trọ cao cấp ngay trung tâm Quận 8");
        p.setAddressNumber("341 Bùi Minh Trực");
        p.setOwner(owner);
        p.setWard(ward);
        p.setCategory(category);
        p.setAcreage(12.2);
        p.setDescription("Nothing");
        p.setNumberOfRooms(2);

        p.setSlug(SlugUtil.makeSlug(category.getName() + " " + p.getTitle()));

        List<PropertyImage>  propertyImages = new ArrayList<>();
        // Ảnh 1 (Làm ảnh bìa)
        PropertyImage img1 = new PropertyImage();
        img1.setImageUrl("https://tromoi.com/uploads/images/sample1.jpg");
        img1.setIsPrimary(true);
        img1.setProperty(p); // QUAN TRỌNG: Phải gán p vào đây để JPA biết property_id
        propertyImages.add(img1);

        // Ảnh 2 (Ảnh thường)
        PropertyImage img2 = new PropertyImage();
        img2.setImageUrl("https://tromoi.com/uploads/images/sample2.jpg");
        img2.setIsPrimary(false);
        img2.setProperty(p);
        propertyImages.add(img2);

        p.setImages(propertyImages);

        propertyRepository.save(p);
    }
}

