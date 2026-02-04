package com.pms.propertymanagement.service;

import com.pms.propertymanagement.dto.response.CategoryResponse;
import com.pms.propertymanagement.entity.Category;

import java.util.List;


public interface CategoryService {
    List<CategoryResponse> findAll();
}
