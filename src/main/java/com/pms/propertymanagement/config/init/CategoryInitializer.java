package com.pms.propertymanagement.config.init;

import com.pms.propertymanagement.entity.Category;
import com.pms.propertymanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    public void init() {
        if (!categoryRepository.findAll().isEmpty()) return;

        categoryRepository.saveAll(List.of(
                new Category("Nhà trọ"),
                new Category("Nhà nguyên căn"),
                new Category("Căn hộ"),
                new Category("Ký túc xá")
        ));
    }
}
