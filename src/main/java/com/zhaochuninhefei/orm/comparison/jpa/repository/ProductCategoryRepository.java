package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商品分类 Repository
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
}
