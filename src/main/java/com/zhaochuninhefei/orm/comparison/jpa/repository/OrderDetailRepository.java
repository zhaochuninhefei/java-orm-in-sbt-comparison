package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单明细 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
