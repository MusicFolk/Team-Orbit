package com.orbit.team.service;

import com.orbit.team.entity.Category;

import java.util.List;

public interface CategoryService {

    List<Category> getAllCategories();

    Category getCategoryById(Long id);
}
