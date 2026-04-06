package com.webbandoan.repository;

import com.webbandoan.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Modifying
    @Query("DELETE FROM OrderDetail od WHERE od.food.id = :foodId")
    void deleteByFoodId(@Param("foodId") Long foodId);
}