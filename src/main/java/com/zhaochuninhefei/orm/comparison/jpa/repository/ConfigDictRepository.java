package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.ConfigDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 配置字典 Repository
 */
@Repository
public interface ConfigDictRepository extends JpaRepository<ConfigDict, Long> {
}
