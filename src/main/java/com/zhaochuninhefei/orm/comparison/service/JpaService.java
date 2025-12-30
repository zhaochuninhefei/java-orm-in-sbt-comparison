package com.zhaochuninhefei.orm.comparison.service;

import com.zhaochuninhefei.orm.comparison.jpa.entity.UserProfile;
import com.zhaochuninhefei.orm.comparison.jpa.repository.UserProfileRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * JPA相关业务Service
 * 负责JPA相关的数据库操作
 */
@Slf4j
@Service
@SuppressWarnings({"SameParameterValue", "java:S1192", "DuplicatedCode"})
public class JpaService {

    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;

    @SuppressWarnings("unused")
    @Value("${spring.jpa.hibernate.jdbc.batch_size:100}")
    private int batchSize;

    private final Random random = new Random();

    private static final String[] DEPARTMENTS = {"研发部", "产品部", "市场部", "销售部", "人力资源部", "财务部", "运营部", "客服部"};
    private static final String[] POSITIONS = {"实习生", "专员", "主管", "经理", "总监", "VP"};

    public JpaService(UserProfileRepository userProfileRepository, EntityManager entityManager) {
        this.userProfileRepository = userProfileRepository;
        this.entityManager = entityManager;
    }

    /**
     * 批量插入用户数据
     *
     * @param insertCount 要插入的数据量
     * @return 实际插入的数据量
     */
    @Transactional
    public int insertUserProfiles(int insertCount) {
        // 生成并插入用户数据
        for (int i = 1; i <= insertCount; i++) {
            UserProfile profile = generateUserProfile(i);
            userProfileRepository.save(profile);
            // 每达到 batch_size 数量，就 flush 并 clear 一次
            if (i % batchSize == 0) {
                // flush 将数据同步到数据库（执行批量 SQL）
                entityManager.flush();
                // clear 清除一级缓存，防止内存堆积，也防止对象重复更新
                entityManager.clear();
            }
        }
        // 循环结束后，处理剩余的数据
        entityManager.flush();
        entityManager.clear();
        return insertCount;
    }

    /**
     * 根据主键更新用户数据
     * 随机选择一条记录进行更新
     *
     * @return 影响行数
     */
    @Transactional
    public int updateUserProfileByPk() {
        // 随机选择一个ID（假设ID从1开始连续递增）
        long randomId = 1 + random.nextLong(100000L);
        // 查询该记录
        return userProfileRepository.findById(randomId)
                .map(profile -> {
                    // 更新字段值
                    profile.setAge(20 + random.nextInt(40));
                    profile.setGender(random.nextInt(2) + 1);
                    profile.setStatus(random.nextBoolean() ? 1 : 0);
                    profile.setDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)]);
                    profile.setPosition(POSITIONS[random.nextInt(POSITIONS.length)]);
                    profile.setSalary(BigDecimal.valueOf(5000 + random.nextDouble() * 45000).setScale(2, RoundingMode.HALF_UP));
                    profile.setDescription("更新后的描述信息 - " + System.currentTimeMillis());
                    profile.setScore(BigDecimal.valueOf(60 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP));
                    profile.setLevel(random.nextInt(10) + 1);
                    // 保存更新
                    userProfileRepository.save(profile);
                    return 1; // 影响行数
                })
                .orElse(0);
    }

    /**
     * 生成用户数据
     *
     * @param index 用户索引
     * @return 用户实体
     */
    private UserProfile generateUserProfile(int index) {
        UserProfile profile = new UserProfile();
        profile.setUsername("jpa_user_" + String.format("%06d", index));
        profile.setEmail("jpa_user_" + index + "@example.com");
        profile.setPhone("1" + String.format("%010d", Math.abs(random.nextLong() % 10000000000L)));
        profile.setAge(20 + random.nextInt(40));
        profile.setGender(random.nextInt(2) + 1);
        profile.setStatus(random.nextBoolean() ? 1 : 0);
        profile.setDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)]);
        profile.setPosition(POSITIONS[random.nextInt(POSITIONS.length)]);
        profile.setSalary(BigDecimal.valueOf(5000 + random.nextDouble() * 45000).setScale(2, RoundingMode.HALF_UP));
        profile.setDescription("这是JPA用户" + index + "的描述信息");
        profile.setScore(BigDecimal.valueOf(60 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP));
        profile.setLevel(random.nextInt(10) + 1);

        return profile;
    }
}