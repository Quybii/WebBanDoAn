package com.webbandoan.service;

import com.webbandoan.entity.Category;
import com.webbandoan.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service: Category.
 * Logic nghiệp vụ liên quan danh mục món ăn (trang chủ, lọc món, ...).
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Lấy tất cả danh mục, sắp theo tên (dùng cho menu, dropdown lọc).
     */
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    /**
     * Lấy danh mục theo id (admin dùng).
     */
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
