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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;

    @Override
    public void run(String... args) throws Exception {
        ensureUsers();
        if (roomRepository.count() == 0) {
            seedData();
        }
    }

    private void ensureUsers() {
        createUserIfMissing("host_user", "HOST");
        createUserIfMissing("host_01", "HOST");
        createUserIfMissing("tenant_user", "TENANT");
        createUserIfMissing("tenant_01", "TENANT");
        createUserIfMissing("tenant_02", "TENANT");
        createUserIfMissing("tenant_03", "TENANT");
    }

    private void createUserIfMissing(String username, String role) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword("password123");
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    private void seedData() {
        User host = userRepository.findByUsername("host_user").orElseThrow();

        Category studio = new Category(null, "Studio");
        Category apartment = new Category(null, "Apartment");
        categoryRepository.saveAll(Arrays.asList(studio, apartment));

        Property property = Property.builder()
                .name("Sunshine Building")
                .addressNumber("123 Main Street")
                .wardCode("W01")
                .owner(host)
                .description("Luxury apartments in city center")
                .build();
        propertyRepository.save(property);

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

        System.out.println("Sample data seeded successfully!");
    }
}
