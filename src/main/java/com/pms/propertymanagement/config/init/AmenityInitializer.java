package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Amenity;
import com.pms.propertymanagement.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AmenityInitializer {

    private final AmenityRepository amenityRepository;

    public void init() {
        if (amenityRepository.findAll().isEmpty()) {
            List<Amenity> amenities = List.of(
                    new Amenity("Wifi", "fa-solid fa-wifi"),
                    new Amenity( "Vệ sinh trong", "fa-solid fa-toilet"),
                    new Amenity( "Phòng tắm", "fa-solid fa-shower"),
                    new Amenity( "Bình nóng lạnh", "fa-solid fa-temperature-arrow-up"),
                    new Amenity( "Kệ bếp", "fa-solid fa-kitchen-set"),
                    new Amenity( "Điều hòa", "fa-solid fa-snowflake"),
                    new Amenity( "Giường nệm", "fa-solid fa-bed"),
                    new Amenity( "Bãi để xe riêng", "fa-solid fa-motorcycle"),
                    new Amenity( "Tủ quần áo", "fa-solid fa-shirt"),
                    new Amenity( "Camera an ninh", "fa-solid fa-video")
            );
            amenityRepository.saveAll(amenities);
        }
    }



}
