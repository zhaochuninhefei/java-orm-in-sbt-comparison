package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 客户 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
