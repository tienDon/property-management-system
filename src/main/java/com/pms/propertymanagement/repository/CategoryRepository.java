package com.pms.propertymanagement.repository;

import com.pms.propertymanagement.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long> {
//    List<Category> findCategories();
    Category findByName(String name);


}
