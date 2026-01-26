package com.pms.propertymanagement.config;

import com.pms.propertymanagement.entity.*;
import com.pms.propertymanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {

        Ward ward = wardRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ward found in DB"));

        // 2️⃣ USER
        User host = new User();
        host.setUsername("host_user");
        host.setPassword("password123");
        host.setIsActive(true);

        Role hostRole = new Role();
        hostRole.setName("ROLE_HOST");
        roleRepository.save(hostRole);

        host.getRoles().add(hostRole);
        userRepository.save(host);

        // 3️⃣ CATEGORY
        Category studio = new Category();
        studio.setName("Studio");

        Category apartment = new Category();
        apartment.setName("Apartment");

        categoryRepository.saveAll(Arrays.asList(studio, apartment));

        // 4️⃣ PROPERTY #1
        Property sunshine = Property.builder()
                .name("Sunshine Building")
                .addressNumber("123 Main Street")
                .ward(ward)   // ✅ FIX
                .owner(host)
                .description("Luxury apartments in city center")
                .build();
        propertyRepository.save(sunshine);

        Room room101 = Room.builder()
                .roomNumber("101")
                .property(sunshine)
                .category(studio)
                .price(5_000_000.0)
                .area(30.0)
                .maxPeople(2)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        Room room102 = Room.builder()
                .roomNumber("102")
                .property(sunshine)
                .category(apartment)
                .price(8_000_000.0)
                .area(50.0)
                .maxPeople(4)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        roomRepository.saveAll(Arrays.asList(room101, room102));

        // 5️⃣ PROPERTY #2
        Property minhTam = Property.builder()
                .name("Nhà trọ Minh Tâm")
                .addressNumber("456 Cách Mạng Tháng 8")
                .ward(ward)   // ✅ FIX
                .owner(host)
                .description("Nhà trọ giá rẻ, gần trung tâm")
                .build();
        propertyRepository.save(minhTam);

        Room room201 = Room.builder()
                .roomNumber("201")
                .property(minhTam)
                .category(studio)
                .price(3_500_000.0)
                .area(25.0)
                .maxPeople(2)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        Room room202 = Room.builder()
                .roomNumber("202")
                .property(minhTam)
                .category(studio)
                .price(4_200_000.0)
                .area(28.0)
                .maxPeople(3)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        roomRepository.saveAll(Arrays.asList(room201, room202));

        System.out.println(">>> DataSeeder completed!");
    }
}
