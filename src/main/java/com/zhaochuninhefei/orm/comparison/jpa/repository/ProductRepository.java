package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
