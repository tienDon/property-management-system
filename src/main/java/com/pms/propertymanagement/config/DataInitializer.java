package com.pms.propertymanagement.config;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static com.pms.propertymanagement.utils.SlugUtil.makeSlug;

@Component

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




    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("Hệ thống đang khởi tạo");
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("USER");
        createRoleIfNotFound("OWNER");

        if(userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("123");
            admin.setFullName("system admin");

            Role adminRole = roleRepository.findByName("ADMIN").get();
            admin.getRoles().add(adminRole);

            userRepository.save(admin);
        }
        initSampleUser();
        initLocations();
        initCategories();
        initSampleProperty();


        System.out.println("Hệ thống đã sẵn sàng");
    }

    private void createRoleIfNotFound(String name){
        if(roleRepository.findByName(name).isEmpty()){
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }

    private void initLocations(){
        if(provinceRepository.findAll().isEmpty()){
            Province hcm = new Province();
            hcm.setCode("79");
            hcm.setName("Thành phố Hồ Chí Minh");
//            provinceRepository.save(hcm);

            Ward p = new Ward();
            p.setCode("27610");
            p.setName("Phường Phước Long A");
            p.setProvince(hcm);
            hcm.getWards().add(p);
//            wardRepository.save(p);
            provinceRepository.save(hcm);


        }
    }


    private void initCategories(){
        if(categoryRepository.findAll().isEmpty()){
            categoryRepository.save(new Category("Nhà trọ"));
            categoryRepository.save(new Category("Căn hộ"));
            categoryRepository.save(new Category("Ký túc xá"));
        }
    }

    private void initSampleProperty(){
        if (propertyRepository.findAll().isEmpty()) {
            User owner = userRepository.findByUsername("owner1").orElse(null);
//            System.out.println(admin.getFullName());
            Ward ward = wardRepository.findById("27610").orElse(null);
            Category category = categoryRepository.findByName("Nhà trọ");

            if (owner != null && ward != null && category != null) {
//                System.out.println(category.getName() + " " + ward.getName() + " " + admin.getFullName());

                Property p = new Property();
                p.setName("Nhà trọ 341");
                p.setTitle("Phòng trọ cao cấp ngay trung tâm Quận 8 full nội thất");
                p.setAddressNumber("341 Bùi Minh Trực");
                p.setDescription("Phòng mới xây, giờ giấc tự do, có bảo vệ...");
                p.setOwner(owner);
                p.setWard(ward);
                p.setCategory(category);

                // 2. Tạo Slug chuẩn SEO
                String rawSlugSource = category.getName() + " " + p.getTitle() + " " + ward.getName() + " " + ward.getProvince().getName();
                p.setSlug(makeSlug(rawSlugSource));

                // 3. Tạo danh sách phòng (Rooms)
                Room r1 = new Room();
                r1.setRoomNumber("101");
                r1.setPrice(3500000.0);
                r1.setArea(25.0);
                r1.setProperty(p); // Quan trọng để map ID

                Room r2 = new Room();
                r2.setRoomNumber("102");
                r2.setPrice(4200000.0);
                r2.setArea(32.0);
                r2.setProperty(p);

                p.setRooms(java.util.List.of(r1, r2));

                // 4. Tạo hình ảnh (Images)
                PropertyImage img = new PropertyImage();
                img.setImageUrl("https://tromoi.com/uploads/static/phong_tro_hcm/Quan_8/19-1-12-lydaothanh/19_1_12_ly_dao_thanh3.jpeg");
                img.setIsPrimary(true);
                img.setProperty(p);

                p.setImages(java.util.List.of(img));

                // 5. Lưu xuống DB
                propertyRepository.save(p);
                System.out.println(">>> Đã tạo bài đăng mẫu: " + p.getSlug());
            }

        }
    }

    private void initSampleUser(){
        if (userRepository.findByUsername("owner1").isEmpty()){
            User user = new User();
            user.setUsername("owner1");
            user.setPassword("123");
            user.setFullName("owner1");

            Role role = roleRepository.findByName("OWNER").get();
            user.getRoles().add(role);

            userRepository.save(user);
        }
    }
}
