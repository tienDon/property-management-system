package com.pms.propertymanagement.service.impl;

import com.pms.propertymanagement.entity.Category;
import com.pms.propertymanagement.repository.CategoryRepository;
import com.pms.propertymanagement.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAllCategory() {
        return categoryRepository.findAll();
    }
}
