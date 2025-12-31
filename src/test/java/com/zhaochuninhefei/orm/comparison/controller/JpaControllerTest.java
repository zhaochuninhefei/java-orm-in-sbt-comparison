package com.zhaochuninhefei.orm.comparison.controller;

import com.zhaochuninhefei.orm.comparison.controller.base.BaseControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * JPA Controller API 测试
 * 使用 MockMvc 测试真实的 HTTP API
 */
@SuppressWarnings({"unused", "ObviousNullCheck", "java:S5845", "SameParameterValue"})
@DisplayName("JPA Controller API 测试")
class JpaControllerTest extends BaseControllerTest {

    @Test
    @DisplayName("测试 JPA 批量插入 API - 插入10000条数据")
    void testInsertApiWith10000Records() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = createMockMvc();

        // 恢复数据
        RestoreResult restoreResult = restoreUserData(mockMvc);

        // 执行测试
        System.out.println("========================================");
        System.out.println("第二步：执行 JPA 批量插入 API");
        System.out.println("========================================");

        String requestBody = "{\"insertCount\": 10000}";
        System.out.println("请求体: " + requestBody);
        System.out.println();

        // JVM 热机
        performWarmup(mockMvc, "/api/jpa/insert", requestBody, "testInsertApiWith10000Records");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/insert", requestBody, "testInsertApiWith10000Records", 3);

        // 验证结果
        for (int i = 0; i < 3; i++) {
            assertNotNull(stats.counts[i], "返回结果不应为空");
            assertEquals(10000, stats.counts[i], "插入件数应为10000");
        }

        // 输出统计
        stats.printStatistics();
    }

    @Test
    @DisplayName("测试 JPA 主键更新 API")
    void testUpdateByPk() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = createMockMvc();

        // 恢复数据
        RestoreResult restoreResult = restoreUserData(mockMvc);

        // 执行测试
        System.out.println("========================================");
        System.out.println("第二步：执行 JPA 主键更新 API");
        System.out.println("========================================");

        // JVM 热机
        performWarmup(mockMvc, "/api/jpa/update/pk", null, "testUpdateByPk");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/update/pk", null, "testUpdateByPk", 3);

        // 验证结果
        for (int i = 0; i < 3; i++) {
            assertNotNull(stats.counts[i], "返回结果不应为空");
            assertEquals(1, stats.counts[i], "更新件数应为1");
        }

        // 输出统计
        stats.printStatistics();
    }

    @Test
    @DisplayName("测试 JPA 条件更新 API")
    void testUpdateByCondition() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = createMockMvc();

        // 恢复数据
        RestoreResult restoreResult = restoreUserData(mockMvc);

        // 执行测试
        System.out.println("========================================");
        System.out.println("第二步：执行 JPA 条件更新 API");
        System.out.println("========================================");

        String requestBody = "{\"level\": 3}";
        System.out.println("请求体: " + requestBody);
        System.out.println();

        // JVM 热机
        performWarmup(mockMvc, "/api/jpa/update/condition", requestBody, "testUpdateByCondition");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/update/condition", requestBody, "testUpdateByCondition", 3);

        // 验证结果
        for (int i = 0; i < 3; i++) {
            assertNotNull(stats.counts[i], "返回结果不应为空");
            assertEquals(10000, stats.counts[i], "更新件数应为10000");
        }

        // 输出统计
        stats.printStatistics();
    }

    @Test
    @DisplayName("测试 JPA 分页查询 API")
    void testPageQuery() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = createMockMvc();

        // 准备请求体
        String requestBody = """
                {
                    "pageNum": 1,
                    "pageSize": 100,
                    "orderStatus": [1,2,3],
                    "regionCode": "REG00003",
                    "minActualPriceSum": 100
                }
                """;

        System.out.println("========================================");
        System.out.println("测试 JPA 分页查询 API");
        System.out.println("========================================");
        System.out.println("请求体: " + requestBody);
        System.out.println();

        // JVM 热机
        performWarmup(mockMvc, "/api/jpa/query/page", requestBody, "testPageQuery");

        // 正式测试（不解析响应为整数）
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/query/page", requestBody, "testPageQuery", 3, false);

        // 输出统计
        stats.printStatistics();
    }

    @Test
    @DisplayName("测试 JPA 全表查询 API")
    void testQueryAll() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = createMockMvc();

        System.out.println("========================================");
        System.out.println("测试 JPA 全表查询 API");
        System.out.println("========================================");
        System.out.println("目标表: config_dict");
        System.out.println();

        // JVM 热机
        performWarmup(mockMvc, "/api/jpa/query/all", null, "testQueryAll");

        // 正式测试（不解析响应为整数）
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/query/all", null, "testQueryAll", 3, false);

        // 输出统计
        stats.printStatistics();
    }
}
