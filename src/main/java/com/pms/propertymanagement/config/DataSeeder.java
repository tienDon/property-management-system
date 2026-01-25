package com.pms.propertymanagement.config;

import com.pms.propertymanagement.entity.Category;
import com.pms.propertymanagement.entity.Property;
import com.pms.propertymanagement.entity.Room;
import com.pms.propertymanagement.entity.User;
import com.pms.propertymanagement.repository.CategoryRepository;
import com.pms.propertymanagement.repository.PropertyRepository;
import com.pms.propertymanagement.repository.RoomRepository;
import com.pms.propertymanagement.repository.UserRepository;
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

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // 1. Create Host/Owner
        User host = new User();
        host.setUsername("host_user");
        host.setPassword("password123"); // In real app, use BCrypt
        host.setRole("HOST");
        host.setActive(true);
        userRepository.save(host);

        // 2. Create Categories
        Category studio = new Category(null, "Studio");
        Category apartment = new Category(null, "Apartment");
        categoryRepository.saveAll(Arrays.asList(studio, apartment));

        // 3. Create Property (Building)
        Property property = Property.builder()
                .name("Sunshine Building")
                .addressNumber("123 Main Street")
                .wardCode("W01")
                .owner(host)
                .description("Luxury apartments in city center")
                .build();
        propertyRepository.save(property);

        // 4. Create Rooms
        Room room101 = Room.builder()
                .roomNumber("101")
                .property(property)
                .category(studio)
                .price(5000000.0)
                .area(30.0)
                .maxPeople(2)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        Room room102 = Room.builder()
                .roomNumber("102")
                .property(property)
                .category(apartment)
                .price(8000000.0)
                .area(50.0)
                .maxPeople(4)
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        roomRepository.saveAll(Arrays.asList(room101, room102));

        System.out.println(">>> Sample data seeded successfully!");
        System.out.println(">>> Room 101 ID: " + room101.getId());
        System.out.println(">>> Room 102 ID: " + room102.getId());
    }
}
