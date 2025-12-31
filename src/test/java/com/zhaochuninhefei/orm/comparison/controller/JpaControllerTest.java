package com.zhaochuninhefei.orm.comparison.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JPA Controller API 测试
 * 使用 MockMvc 测试真实的 HTTP API
 */
@SuppressWarnings({"unused", "ObviousNullCheck", "java:S5845", "SameParameterValue"})
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JPA Controller API 测试")
class JpaControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    /**
     * 测试结果统计类
     */
    private static class TestStatistics {
        long[] durations;
        int[] counts;
        String operationName;
        long totalTestStartTime;
        long totalTestEndTime;

        TestStatistics(int iterations, String operationName) {
            this.durations = new long[iterations];
            this.counts = new int[iterations];
            this.operationName = operationName;
        }

        void setDuration(int index, long duration) {
            this.durations[index] = duration;
        }

        void setCount(int index, int count) {
            this.counts[index] = count;
        }

        long getTotalDuration() {
            long total = 0;
            for (long d : durations) {
                total += d;
            }
            return total;
        }

        long getTotalRecords() {
            long total = 0;
            for (int c : counts) {
                total += c;
            }
            return total;
        }

        double getAvgDuration() {
            return getTotalDuration() / (double) durations.length;
        }

        double getMinDuration() {
            long min = durations[0];
            for (long d : durations) {
                if (d < min) min = d;
            }
            return min;
        }

        double getMaxDuration() {
            long max = durations[0];
            for (long d : durations) {
                if (d > max) max = d;
            }
            return max;
        }

        double getAvgTimePerRecord() {
            return getTotalDuration() / (double) getTotalRecords();
        }

        double getThroughput() {
            return getTotalRecords() * 1000.0 / getTotalDuration();
        }

        void printStatistics() {
            long totalDuration = getTotalDuration();
            long totalRecords = getTotalRecords();

            // 输出统计信息
            System.out.println("✓ " + operationName + "完成");
            System.out.println();
            System.out.println("性能统计（基于3次正式测试）:");
            System.out.println("========================================");
            System.out.println("   - 总" + operationName + "件数: " + totalRecords + " 条");
            System.out.println("   - 总执行时间: " + totalDuration + "ms");
            System.out.println("   - 平均执行时间: " + String.format("%.2f", getAvgDuration()) + "ms");
            System.out.println("   - 最小执行时间: " + (long) getMinDuration() + "ms");
            System.out.println("   - 最大执行时间: " + (long) getMaxDuration() + "ms");
            System.out.println();
            System.out.println("性能指标:");
            System.out.println("   - 平均每条耗时: " + String.format("%.4f", getAvgTimePerRecord()) + " ms/条");
            System.out.println("   - 吞吐量: " + String.format("%.2f", getThroughput()) + " 条/秒");
            System.out.println("========================================");
        }
    }

    /**
     * 创建 MockMvc 实例
     */
    private MockMvc createMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 恢复 user_profile 表数据
     *
     * @param mockMvc MockMvc 实例
     * @return 恢复的数据件数和耗时
     */
    private RestoreResult restoreUserData(MockMvc mockMvc) throws Exception {
        System.out.println("========================================");
        System.out.println("第一步：恢复 user_profile 表数据");
        System.out.println("========================================");

        long restoreStartTime = System.currentTimeMillis();

        MvcResult restoreResult = mockMvc.perform(post("/api/data/restore/user_profile"))
                .andExpect(status().isOk())
                .andReturn();

        long restoreEndTime = System.currentTimeMillis();
        long restoreDuration = restoreEndTime - restoreStartTime;

        // 获取恢复结果
        String restoreResponseContent = restoreResult.getResponse().getContentAsString();
        int restoredCount = Integer.parseInt(restoreResponseContent.trim());

        System.out.println("✓ user_profile 表数据恢复完成");
        System.out.println("  - 恢复件数: " + restoredCount);
        System.out.println("  - 耗时: " + restoreDuration + "ms");
        System.out.println();

        return new RestoreResult(restoredCount, restoreDuration);
    }

    /**
     * 恢复结果
     */
    private record RestoreResult(int count, long duration) {
    }

    /**
     * 执行 JVM 热机
     *
     * @param mockMvc       MockMvc 实例
     * @param url           API URL
     * @param requestBody   请求体
     * @param operationName 操作名称（插入/更新）
     */
    private void performWarmup(MockMvc mockMvc, String url, String requestBody, String operationName) throws Exception {
        System.out.println("--- JVM 热机阶段（5次，不计入统计）---");

        for (int i = 1; i <= 5; i++) {
            System.out.println("热机第 " + i + " 次...");

            MockHttpServletRequestBuilder requestBuilder = post(url)
                    .contentType(MediaType.APPLICATION_JSON);

            if (requestBody != null) {
                requestBuilder.content(requestBody);
            }

            // 执行 API 调用
            MvcResult warmupResult = mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andReturn();

            // 获取结果
            String warmupResponseContent = warmupResult.getResponse().getContentAsString();
            System.out.println("  - " + operationName + "结果: " + warmupResponseContent);
        }

        System.out.println("✓ JVM 热机完成");
        System.out.println();
    }

    /**
     * 执行正式测试
     *
     * @param mockMvc       MockMvc 实例
     * @param url           API URL
     * @param requestBody   请求体
     * @param operationName 操作名称（插入/更新/查询）
     * @param iterations    测试次数
     * @return 测试统计数据
     */
    private TestStatistics performFormalTest(MockMvc mockMvc, String url, String requestBody,
                                             String operationName, int iterations) throws Exception {
        return performFormalTest(mockMvc, url, requestBody, operationName, iterations, true);
    }

    /**
     * 执行正式测试（支持查询类型，不解析响应体为整数）
     *
     * @param mockMvc            MockMvc 实例
     * @param url                API URL
     * @param requestBody        请求体
     * @param operationName      操作名称（插入/更新/查询）
     * @param iterations         测试次数
     * @param parseResponseAsInt 是否将响应解析为整数
     * @return 测试统计数据
     */
    private TestStatistics performFormalTest(MockMvc mockMvc, String url, String requestBody,
                                             String operationName, int iterations, boolean parseResponseAsInt) throws Exception {
        System.out.println("--- 正式测试阶段（" + iterations + "次，计入统计）---");

        TestStatistics stats = new TestStatistics(iterations, operationName);
        stats.totalTestStartTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            System.out.println("测试第 " + (i + 1) + " 次...");

            long startTime = System.currentTimeMillis();

            MockHttpServletRequestBuilder requestBuilder = post(url)
                    .contentType(MediaType.APPLICATION_JSON);

            if (requestBody != null) {
                requestBuilder.content(requestBody);
            }

            // 执行 API 调用
            MvcResult testResult = mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andReturn();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            stats.setDuration(i, duration);

            // 获取结果
            String testResponseContent = testResult.getResponse().getContentAsString();

            if (parseResponseAsInt) {
                // 解析为整数（插入/更新操作）
                int actualCount = Integer.parseInt(testResponseContent.trim());
                stats.setCount(i, actualCount);
                System.out.println("  - " + operationName + "件数: " + actualCount);
            } else {
                // 查询操作，不解析具体数量，设置为1表示成功
                stats.setCount(i, 1);
                System.out.println("  - 查询耗时: " + duration + "ms");

                // 第一次输出响应内容预览
                if (i == 0) {
                    String preview = testResponseContent.substring(0, Math.min(200, testResponseContent.length()));
                    System.out.println("  - 响应内容预览: " + preview + "...");
                }
            }

            System.out.println("  - 执行时间: " + duration + "ms");
        }

        stats.totalTestEndTime = System.currentTimeMillis();

        System.out.println("✓ 正式测试完成");
        System.out.println();

        return stats;
    }

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
        performWarmup(mockMvc, "/api/jpa/insert", requestBody, "插入");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/insert", requestBody, "插入", 3);

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
        performWarmup(mockMvc, "/api/jpa/update/pk", null, "主键更新");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/update/pk", null, "主键更新", 3);

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
        performWarmup(mockMvc, "/api/jpa/update/condition", requestBody, "条件更新");

        // 正式测试
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/update/condition", requestBody, "条件更新", 3);

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
        performWarmup(mockMvc, "/api/jpa/query/page", requestBody, "查询");

        // 正式测试（不解析响应为整数）
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/query/page", requestBody, "查询", 3, false);

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
        performWarmup(mockMvc, "/api/jpa/query/all", null, "查询");

        // 正式测试（不解析响应为整数）
        TestStatistics stats = performFormalTest(mockMvc, "/api/jpa/query/all", null, "查询", 3, false);

        // 输出统计
        stats.printStatistics();
    }
}
