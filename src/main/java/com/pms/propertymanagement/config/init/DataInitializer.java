package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pms.propertymanagement.utils.SlugUtil.makeSlug;

@Component
@RequiredArgsConstructor

public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    private final RoleInitializer roleInitializer;
    private final UserInitializer userInitializer;
    private final LocationInitializer locationInitializer;
    private final CategoryInitializer categoryInitializer;
    private final PropertyInitializer propertyInitializer;
    private final AmenityInitializer amenityInitializer;
    private final SurroundingInitializer surroundingInitializer;
    private final TargetTenantInitializer targetTenantInitializer;
    private final TenantInitializer tenantInitializer;
    private final ServiceInitializer serviceInitializer;
    private final ContactInitializer contactInitializer;
    private final RoomInitializer roomInitializer;
    private final ContractInitializer contractInitializer;
    private final PostingPackageInitializer postingPackageInitializer;
    private final ManagementPlanInitializer managementPlanInitializer;
    private final PostPackageInitializer postPackageInitializer;
    private final PostInitializer postInitializer;
    private final OwnerDataInitializer ownerDataInitializer;
    private final LargeScaleDataInitializer largeScaleDataInitializer;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Hệ thống đang khởi tạo");

        roleInitializer.init();
        userInitializer.init();
        locationInitializer.init();
        categoryInitializer.init();
        amenityInitializer.init();
        surroundingInitializer.init();
        targetTenantInitializer.init();
        propertyInitializer.init();

        Category defaultCategory = categoryRepository.findByName("Nhà trọ");
        if (defaultCategory == null) {
            List<Category> categories = categoryRepository.findAll();
            defaultCategory = categories.isEmpty() ? null : categories.get(0);
        }
        if (defaultCategory != null) {
            List<Property> invalidCategoryProperties = propertyRepository.findPropertiesWithInvalidCategory();
            if (!invalidCategoryProperties.isEmpty()) {
                for (Property p : invalidCategoryProperties) {
                    p.setCategory(defaultCategory);
                }
                propertyRepository.saveAll(invalidCategoryProperties);
            }
        }

        serviceInitializer.init();
        roomInitializer.init();
        tenantInitializer.init();
        contactInitializer.init();
        contractInitializer.init();
        postingPackageInitializer.init();
        managementPlanInitializer.init();
        postPackageInitializer.init();
        
        // NEW ARCHITECTURE: Initialize Posts for Properties
        postInitializer.init();

        // Sample data: owner1 wallet + Enterprise subscription
        ownerDataInitializer.init();

        // Large scale data for statistics
        largeScaleDataInitializer.init();

        System.out.println("Hệ thống đã sẵn sàng");
    }}




