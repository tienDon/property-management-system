package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
        serviceInitializer.init();
        roomInitializer.init();
        tenantInitializer.init();
        contactInitializer.init();
        contractInitializer.init();


        System.out.println("Hệ thống đã sẵn sàng");
    }}




