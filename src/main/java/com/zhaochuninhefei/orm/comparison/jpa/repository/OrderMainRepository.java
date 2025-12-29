package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.OrderMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 订单主表 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {
}
