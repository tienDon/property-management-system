package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Surrounding;
import com.pms.propertymanagement.repository.SurroundingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SurroundingInitializer {

    private final SurroundingRepository surroundingRepository;

    public void init(){
        if(!surroundingRepository.findAll().isEmpty()) return;

        List<Surrounding> surroundings = List.of(
                new Surrounding("Chợ", "fa-solid fa-shop" ),
                new Surrounding( "Siêu thị", "fa-solid fa-cart-shopping" ),
                new Surrounding( "Bệnh viện", "fa-solid fa-hospital" ),
                new Surrounding("Trường học", "fa-solid fa-school" ),
                new Surrounding( "Công viên", "fa-solid fa-tree" ),
                new Surrounding( "Bến xe Bus", "fa-solid fa-bus" ),
                new Surrounding( "Phòng Gym", "fa-solid fa-dumbbell" )
        );
        surroundingRepository.saveAll(surroundings);
    }
}
