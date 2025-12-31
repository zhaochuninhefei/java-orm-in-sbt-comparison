package com.zhaochuninhefei.orm.comparison.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.zhaochuninhefei.orm.comparison.common.Constants;
import com.zhaochuninhefei.orm.comparison.dto.AllQueryResponse;
import com.zhaochuninhefei.orm.comparison.dto.OrderDetailResult;
import com.zhaochuninhefei.orm.comparison.dto.PageQueryRequest;
import com.zhaochuninhefei.orm.comparison.dto.PageQueryResponse;
import com.zhaochuninhefei.orm.comparison.mybatis.dao.PoConfigDictMapper;
import com.zhaochuninhefei.orm.comparison.mybatis.dao.PoOrderMainMapper;
import com.zhaochuninhefei.orm.comparison.mybatis.dao.PoUserProfileMapper;
import com.zhaochuninhefei.orm.comparison.mybatis.po.PoConfigDict;
import com.zhaochuninhefei.orm.comparison.mybatis.po.PoUserProfile;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/*
 * MyBatis Service : 基于MyBatis的测试接口服务
 */
@SuppressWarnings({"DuplicatedCode", "unused"})
@Slf4j
@Service
public class MybatisService {
    private final PoUserProfileMapper poUserProfileMapper;
    private final PoConfigDictMapper poConfigDictMapper;
    private final PoOrderMainMapper poOrderMainMapper;

    @SuppressWarnings("unused")
    @Value("${spring.jpa.hibernate.jdbc.batch_size:100}")
    private int batchSize;

    private final Random random = new Random();

    public MybatisService(PoUserProfileMapper poUserProfileMapper, PoConfigDictMapper poConfigDictMapper, PoOrderMainMapper poOrderMainMapper) {
        this.poUserProfileMapper = poUserProfileMapper;
        this.poConfigDictMapper = poConfigDictMapper;
        this.poOrderMainMapper = poOrderMainMapper;
    }

    /**
     * 批量插入用户数据
     *
     * @param insertCount 要插入的数据量
     * @return 实际插入的数据量
     */
    @Transactional
    public int insertUserProfiles(int insertCount) {
        List<PoUserProfile> data = IntStream.rangeClosed(1, insertCount)
                .mapToObj(this::generatePoUserProfile)
                .toList();
        ListUtils.partition(data, batchSize).forEach(poUserProfileMapper::insertBatch);
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
        var profile = new PoUserProfile();
        profile.setId(randomId);
        profile.setAge(20 + random.nextInt(40));
        profile.setGender((byte) (random.nextInt(2) + 1));
        profile.setStatus((byte) (random.nextBoolean() ? 1 : 0));
        profile.setDepartment(Constants.DEPARTMENTS[random.nextInt(Constants.DEPARTMENTS.length)]);
        profile.setPosition(Constants.POSITIONS[random.nextInt(Constants.POSITIONS.length)]);
        profile.setSalary(BigDecimal.valueOf(5000 + random.nextDouble() * 45000).setScale(2, RoundingMode.HALF_UP));
        profile.setDescription("更新后的描述信息 - " + System.currentTimeMillis());
        profile.setScore(BigDecimal.valueOf(60 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP));
        profile.setLevel(random.nextInt(10) + 1);
        return poUserProfileMapper.updateByPrimaryKeySelective(profile);
    }

    /**
     * 批量更新指定level的用户数据
     * 更新内容：age+1, salary+1000, description前面添加"update"
     *
     * @param level 指定的level
     * @return 影响行数
     */
    @Transactional
    public int updateUserProfilesByLevel(Integer level) {
        var params = new PoUserProfile();
        params.setAge(1);
        params.setSalary(BigDecimal.valueOf(1000));
        params.setDescription("update");
        params.setLevel(level);
        return poUserProfileMapper.updateByLevel(params);
    }

    /**
     * 复杂分页查询
     * 包含CTE、多表JOIN、GROUP BY、HAVING等复杂查询
     *
     * @param request 分页查询请求
     * @return 分页查询响应
     */
    @Transactional(readOnly = true)
    public PageQueryResponse complexPageQuery(PageQueryRequest request) {
        Page<OrderDetailResult> page = PageMethod.startPage(request.getPageNum(), request.getPageSize())
                .setOrderBy("om.create_time desc, om.id desc")
                .doSelectPage(() -> poOrderMainMapper.complexQueryByPage(request.getRegionCode(), request.getMinActualPriceSum()));
        PageQueryResponse response = new PageQueryResponse();
        response.setRecords(page.getResult());
        response.setTotal(page.getTotal());
        response.setPageNum(request.getPageNum());
        response.setPageSize(request.getPageSize());
        response.setTotalPages(page.getPages());
        return response;
    }

    /**
     * 全表查询配置字典数据
     *
     * @return 全表查询响应
     */
    @Transactional(readOnly = true)
    public AllQueryResponse<PoConfigDict> queryAllConfigDict() {
        AllQueryResponse<PoConfigDict> response = new AllQueryResponse<>();
        List<PoConfigDict> records = poConfigDictMapper.selectByExample(null);
        response.setRecords(records);
        response.setTotal((long) records.size());
        return response;
    }

    private PoUserProfile generatePoUserProfile(int index) {
        PoUserProfile profile = new PoUserProfile();
        profile.setUsername("mbt_user_" + String.format("%06d", index));
        profile.setEmail("mbt_user_" + index + "@example.com");
        profile.setPhone("1" + String.format("%010d", Math.abs(random.nextLong() % 10000000000L)));
        profile.setAge(20 + random.nextInt(40));
        profile.setGender((byte) (random.nextInt(2) + 1));
        profile.setStatus((byte) (random.nextBoolean() ? 1 : 0));
        profile.setDepartment(Constants.DEPARTMENTS[random.nextInt(Constants.DEPARTMENTS.length)]);
        profile.setPosition(Constants.POSITIONS[random.nextInt(Constants.POSITIONS.length)]);
        profile.setSalary(BigDecimal.valueOf(5000 + random.nextDouble() * 45000).setScale(2, RoundingMode.HALF_UP));
        profile.setDescription("这是JPA用户" + index + "的描述信息");
        profile.setScore(BigDecimal.valueOf(60 + random.nextDouble() * 40).setScale(2, RoundingMode.HALF_UP));
        profile.setLevel(random.nextInt(10) + 1);

        return profile;
    }
}