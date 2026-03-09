package com.webbandoan.repository;

import com.webbandoan.entity.Order;
import com.webbandoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository: Order (đơn hàng).
 * Dùng để truy vấn bảng orders.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);
}
