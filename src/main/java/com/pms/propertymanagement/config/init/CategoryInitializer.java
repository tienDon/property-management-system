package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Category;
import com.pms.propertymanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    public void init() {
        List<String> requiredNames = List.of(
                "Nhà trọ",
                "Nhà nguyên căn",
                "Căn hộ",
                "Ký túc xá"
        );

        List<Category> toInsert = new ArrayList<>();
        for (String name : requiredNames) {
            if (categoryRepository.findByName(name) == null) {
                toInsert.add(new Category(name));
            }
        }

        if (!toInsert.isEmpty()) {
            categoryRepository.saveAll(toInsert);
        }
    }
}
