package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.*;
import com.pms.propertymanagement.utils.SlugUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PropertyInitializer {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final CategoryRepository categoryRepository;
    private final AmenityRepository amenityRepository;
    private final SurroundingRepository surroundingRepository;
    private final TargetTenantsRepository targetTenantsRepository;

    @Transactional
    public void init() {
        if (!propertyRepository.findAll().isEmpty()) return;

        User owner = userRepository.findByUsername("owner1").orElse(null);
        if (owner == null) return;

        // Load reference data
        Map<String, Category> categories = new HashMap<>();
        categoryRepository.findAll().forEach(c -> categories.put(c.getName(), c));
        
        List<Amenity> allAmenities = amenityRepository.findAll();
        List<Surrounding> allSurroundings = surroundingRepository.findAll();
        List<TargetTenant> allTargets = targetTenantsRepository.findAll();
        List<Ward> allWards = wardRepository.findAll();
        
        if (categories.isEmpty() || allWards.isEmpty()) return;

        Random random = new Random();
        List<Property> properties = new ArrayList<>();

        // 1. NHÀ TRỌ (5 Properties)
        Category catNhaTro = categories.get("Nhà trọ");
        if (catNhaTro != null) {
            properties.add(createProperty(owner, catNhaTro, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà trọ 341", "Phòng trọ cao cấp ngay trung tâm Quận 8", "341 Bùi Minh Trực", 
                12.2, 2500000, 2, "Phòng mới xây có gác lửng, giờ giấc tự do, không chung chủ."));
            
            properties.add(createProperty(owner, catNhaTro, allWards, allAmenities, allSurroundings, allTargets,
                "Trọ Cô Ba", "Phòng trọ giá rẻ cho sinh viên gần ĐH Hutech", "152/12 Điện Biên Phủ", 
                15.0, 3000000, 5, "Cách đại học Hutech 500m, điện nước giá nhà nước, an ninh tốt."));
            
            properties.add(createProperty(owner, catNhaTro, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà trọ Xanh", "Phòng trọ khép kín đầy đủ tiện nghi Bình Thạnh", "25 Nguyễn Xí", 
                20.0, 4500000, 3, "Phòng full nội thất: Máy lạnh, tủ lạnh, giường nệm. Chỉ cần xách vali vào ở."));
            
            properties.add(createProperty(owner, catNhaTro, allWards, allAmenities, allSurroundings, allTargets,
                "Trọ An Nhiên", "Phòng trọ yên tĩnh thoáng mát Quận 7", "45 Lâm Văn Bền", 
                18.0, 3200000, 4, "Khu dân trí cao, yên tĩnh, thích hợp cho nhân viên văn phòng."));
                
            properties.add(createProperty(owner, catNhaTro, allWards, allAmenities, allSurroundings, allTargets,
                "Happy House", "Hệ thống phòng trọ tiện ích Thủ Đức", "102 Lê Văn Việt", 
                25.0, 4000000, 8, "Có thang máy, hầm để xe rộng rãi, bảo vệ 24/7."));
        }

        // 2. NHÀ NGUYÊN CĂN (5 Properties)
        Category catNguyenCan = categories.get("Nhà nguyên căn");
        if (catNguyenCan != null) {
            properties.add(createProperty(owner, catNguyenCan, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà Mặt Tiền Lê Lợi", "Cho thuê nhà nguyên căn mặt tiền kinh doanh", "12 Lê Lợi", 
                80.0, 15000000, 1, "Nhà 1 trệt 2 lầu, mặt tiền đường lớn, thích hợp mở văn phòng hoặc shop."));
            
            properties.add(createProperty(owner, catNguyenCan, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà Hẻm Xe Hơi Q3", "Nhà nguyên căn hẻm xe hơi yên tĩnh Quận 3", "20/5 Võ Văn Tần", 
                60.0, 12000000, 1, "Nhà mới sửa, sạch sẽ, khu vực an ninh, gần chợ và siêu thị."));
                
            properties.add(createProperty(owner, catNguyenCan, allWards, allAmenities, allSurroundings, allTargets,
                "Biệt thự mini Thảo Điền", "Cho thuê biệt thự mini sân vườn Thảo Điền", "15 Quốc Hương", 
                200.0, 35000000, 1, "Không gian xanh mát, hồ bơi riêng, nội thất cao cấp sang trọng."));
            
            properties.add(createProperty(owner, catNguyenCan, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà phố KDC Him Lam", "Nhà phố nguyên căn KDC Him Lam Q7", "Đường số 5", 
                100.0, 18000000, 1, "Khu dân cư cao cấp, đường rộng 12m, gần Lotte Mart."));

            properties.add(createProperty(owner, catNguyenCan, allWards, allAmenities, allSurroundings, allTargets,
                "Nhà cấp 4 Gò Vấp", "Cho thuê nhà cấp 4 có gác lửng Gò Vấp", "120 Quang Trung", 
                40.0, 6000000, 1, "Nhà nhỏ xinh, phù hợp gia đình trẻ, điện nước chính chủ."));
        }

        // 3. CĂN HỘ (5 Properties)
        Category catCanHo = categories.get("Căn hộ");
        if (catCanHo != null) {
            properties.add(createProperty(owner, catCanHo, allWards, allAmenities, allSurroundings, allTargets,
                "Vinhomes Central Park", "Căn hộ 2PN View sông Landmark 81", "208 Nguyễn Hữu Cảnh", 
                75.0, 22000000, 1, "View trực diện sông Sài Gòn, full nội thất cao cấp, tiện ích 5 sao."));
            
            properties.add(createProperty(owner, catCanHo, allWards, allAmenities, allSurroundings, allTargets,
                "Masteri Thảo Điền", "Căn hộ 1PN Masteri Thảo Điền giá tốt", "159 Xa Lộ Hà Nội", 
                50.0, 14000000, 1, "Tầng trung, view thoáng, ngay ga Metro, có gym hồ bơi miễn phí."));
            
            properties.add(createProperty(owner, catCanHo, allWards, allAmenities, allSurroundings, allTargets,
                "Sunrise City", "Căn hộ Sunrise City Quận 7 - 3PN", "23 Nguyễn Hữu Thọ", 
                120.0, 28000000, 1, "Căn góc 3 view, nội thất gỗ tự nhiên, đối diện Lotte Mart."));
            
            properties.add(createProperty(owner, catCanHo, allWards, allAmenities, allSurroundings, allTargets,
                "The Gold View", "Cho thuê căn hộ Gold View Quận 4", "346 Bến Vân Đồn", 
                80.0, 17000000, 1, "Gần trung tâm Q1, view sông, đầy đủ tiện nghi, hồ bơi tràn bờ."));

            properties.add(createProperty(owner, catCanHo, allWards, allAmenities, allSurroundings, allTargets,
                "Ehome 3 Bình Tân", "Căn hộ giá rẻ Ehome 3 full nội thất", "102 Hồ Học Lãm", 
                60.0, 6500000, 1, "Khu chung cư yên tĩnh, nhiều cây xanh, phí quản lý thấp."));
        }

        // 4. KÝ TÚC XÁ (5 Properties)
        Category catKTX = categories.get("Ký túc xá");
        if (catKTX != null) {
            properties.add(createProperty(owner, catKTX, allWards, allAmenities, allSurroundings, allTargets,
                "KTX Cao Cấp Gò Vấp", "Giường tầng cao cấp máy lạnh 24/24 Gò Vấp", "50 Phạm Văn Đồng", 
                15.0, 1800000, 20, "Giường hộp riêng tư, bao điện nước, wifi tốc độ cao."));
            
            properties.add(createProperty(owner, catKTX, allWards, allAmenities, allSurroundings, allTargets,
                "Sleepbox Quận 10", "Sleepbox tiện nghi gần ĐH Bách Khoa", "268 Lý Thường Kiệt", 
                10.0, 2200000, 16, "Mô hình sleepbox hiện đại, khóa từ riêng, bếp chung sạch sẽ."));
            
            properties.add(createProperty(owner, catKTX, allWards, allAmenities, allSurroundings, allTargets,
                "KTX Sinh Viên Làng ĐH", "Ký túc xá giá rẻ khu Làng Đại Học", "Khu phố 6", 
                20.0, 800000, 50, "Môi trường học tập tốt, an ninh, gần trạm xe buýt, giá cực rẻ."));
            
            properties.add(createProperty(owner, catKTX, allWards, allAmenities, allSurroundings, allTargets,
                "Homestay 1990", "Homestay ký túc xá phong cách Vintage", "100 Trần Hưng Đạo", 
                18.0, 1900000, 12, "Không gian chill, ban công rộng, tổ chức tiệc BBQ hàng tuần."));

            properties.add(createProperty(owner, catKTX, allWards, allAmenities, allSurroundings, allTargets,
                "KTX Nữ Bình Thạnh", "Ký túc xá dành riêng cho nữ an ninh", "30 Đường D2", 
                25.0, 1600000, 10, "Chỉ nhận nữ, sạch sẽ, có camera giám sát, giờ giấc 11h đóng cửa."));
        }

        propertyRepository.saveAll(properties);
    }

    private Property createProperty(User owner, Category category, List<Ward> wards,
                                    List<Amenity> allAmenities, List<Surrounding> allSurroundings, 
                                    List<TargetTenant> allTargets,
                                    String name, String title, String address, double acreage, 
                                    int price, int rooms, String desc) {
        Random random = new Random();
        Property p = new Property();
        p.setName(name);
        p.setAddressNumber(address);
        p.setOwner(owner);
        p.setCategory(category);
        p.setAcreage(acreage);
        p.setPrice(price);
        p.setNumberOfRooms(rooms);
        p.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
        p.setUpdatedAt(LocalDateTime.now());
        
        // Random Ward from list
        p.setWard(wards.get(random.nextInt(wards.size())));
        
        // NEW ARCHITECTURE: Property only contains real estate fields
        // Marketing fields (title, slug, description) will be added by PostInitializer

        // Random Amenities (2-5 items)
        if (!allAmenities.isEmpty()) {
            Collections.shuffle(allAmenities);
            p.setAmenities(new HashSet<>(allAmenities.subList(0, Math.min(allAmenities.size(), random.nextInt(4) + 2))));
        }

        // Random Surroundings (2-4 items)
        if (!allSurroundings.isEmpty()) {
            Collections.shuffle(allSurroundings);
            p.setSurroundings(new HashSet<>(allSurroundings.subList(0, Math.min(allSurroundings.size(), random.nextInt(3) + 2))));
        }

        // Random Target Tenants (1-3 items)
        if (!allTargets.isEmpty()) {
            Collections.shuffle(allTargets);
            p.setTargetTenants(new HashSet<>(allTargets.subList(0, Math.min(allTargets.size(), random.nextInt(3) + 1))));
        }

        List<PropertyImage> images = new ArrayList<>();
        // Sample images (Placeholders or real valid URLs if available)
        String[] sampleImages = {
            "https://tromoi.com/uploads/static/phong_tro_hcm/Quan_11/153%20tran%20quy/img_3410.jpg",
            "https://tromoi.com/uploads/static/phong_tro_hcm/Quan_1/54-3-NguyenBinhKhiem/54_3_nguyenbinhkhiem9.jpg",
            "https://tromoi.com/uploads/static/phong-tro-ha-noi/1-Ba-Dinh/Ngo266DoiCan/ngo266doican3.jpg",
            "https://tromoi.com/uploads/static/phong-tro-ha-noi/1-Tay-Ho/pho-tu-lien-duong-au-co/pho_tu_lien1(1).jpg",
            "https://tromoi.com/uploads/guest/1768900126374_13c85297fc2c73722a3d18.jpg"
        };
        
        // Add 2-3 images
        int imgCount = random.nextInt(2) + 2;
        for (int i = 0; i < imgCount; i++) {
            PropertyImage img = new PropertyImage();
            img.setImageUrl(sampleImages[random.nextInt(sampleImages.length)]);
            img.setIsPrimary(i == 0);
            img.setProperty(p);
            images.add(img);
        }
        p.setImages(images);

        return p;
    }
}

