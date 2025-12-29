package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 用户基础信息 Repository
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
