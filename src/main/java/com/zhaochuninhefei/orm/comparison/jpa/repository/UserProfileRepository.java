package com.zhaochuninhefei.orm.comparison.jpa.repository;

import com.zhaochuninhefei.orm.comparison.jpa.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户基础信息 Repository
 */
@SuppressWarnings({"NullableProblems", "unused"})
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 根据level查询所有用户
     *
     * @param level 级别
     * @return 用户列表
     */
    List<UserProfile> findByLevel(Integer level);
}
