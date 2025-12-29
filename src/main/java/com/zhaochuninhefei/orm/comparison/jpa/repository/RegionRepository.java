package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 地区 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
