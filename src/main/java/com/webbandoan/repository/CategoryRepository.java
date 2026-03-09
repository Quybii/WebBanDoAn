package com.webbandoan.repository;

import com.webbandoan.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: Category.
 * Dùng để truy vấn bảng categories (danh mục món ăn).
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Lấy tất cả danh mục, sắp xếp theo tên.
     */
    List<Category> findAllByOrderByNameAsc();
}
