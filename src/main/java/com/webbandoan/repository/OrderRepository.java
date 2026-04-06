package com.webbandoan.repository;

import com.webbandoan.entity.Order;
import com.webbandoan.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository: Order (đơn hàng).
 * Dùng để truy vấn bảng orders.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    @Query("SELECT o FROM Order o JOIN o.orderDetails od WHERE o.user = :user AND od.food.id = :foodId AND (UPPER(o.status) = 'COMPLETED' OR UPPER(o.paymentStatus) = 'COMPLETED') ORDER BY o.orderDate DESC")
    List<Order> findCompletedOrdersContainingFood(@Param("user") User user, @Param("foodId") Long foodId);

        @Query(value = """
                        SELECT CAST(o.order_date AS date) AS report_date, COALESCE(SUM(o.total_amount), 0) AS revenue
                        FROM orders o
                        WHERE (UPPER(o.status) = 'COMPLETED' OR UPPER(o.payment_status) = 'COMPLETED')
                            AND o.order_date >= :startDate
                            AND o.order_date < :endDate
                        GROUP BY CAST(o.order_date AS date)
                        ORDER BY report_date
                        """, nativeQuery = true)
        List<Object[]> findDailyRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                                                                     @Param("endDate") LocalDateTime endDate);

        @Query(value = """
                        SELECT COALESCE(SUM(o.total_amount), 0)
                        FROM orders o
                        WHERE (UPPER(o.status) = 'COMPLETED' OR UPPER(o.payment_status) = 'COMPLETED')
                            AND o.order_date >= :startDate
                            AND o.order_date < :endDate
                        """, nativeQuery = true)
        BigDecimal findTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                                                             @Param("endDate") LocalDateTime endDate);
}
