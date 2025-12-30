package com.zhaochuninhefei.orm.comparison.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JPA Controller API 测试")
class JpaControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    @DisplayName("测试 JPA 批量插入 API - 插入10000条数据")
    void testInsertApiWith10000Records() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // ========== 第一步：恢复 user_profile 表数据 ==========
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
        Integer restoredCount = Integer.valueOf(restoreResponseContent.trim());

        System.out.println("✓ user_profile 表数据恢复完成");
        System.out.println("  - 恢复件数: " + restoredCount);
        System.out.println("  - 耗时: " + restoreDuration + "ms");
        System.out.println();

        // ========== 第二步：JVM 热机 + 执行 JPA 批量插入 API ==========
        System.out.println("========================================");
        System.out.println("第二步：执行 JPA 批量插入 API");
        System.out.println("========================================");

        // 准备请求体
        String requestBody = "{\"insertCount\": 10000}";
        System.out.println("请求体: " + requestBody);
        System.out.println();

        // --- JVM 热机阶段：执行5次（不计入统计）---
        System.out.println("--- JVM 热机阶段（5次，不计入统计）---");

        for (int i = 1; i <= 5; i++) {
            System.out.println("热机第 " + i + " 次...");

            // 执行 API 调用
            MvcResult warmupResult = mockMvc.perform(post("/api/jpa/insert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andReturn();

            // 获取结果
            String warmupResponseContent = warmupResult.getResponse().getContentAsString();
            Integer warmupCount = Integer.valueOf(warmupResponseContent.trim());
            System.out.println("  - 插入件数: " + warmupCount);
        }

        System.out.println("✓ JVM 热机完成");
        System.out.println();

        // --- 正式测试阶段：执行3次（计入统计）---
        System.out.println("--- 正式测试阶段（3次，计入统计）---");

        long[] durations = new long[3];
        int[] counts = new int[3];
        long totalTestStartTime = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            System.out.println("测试第 " + (i + 1) + " 次...");

            long startTime = System.currentTimeMillis();

            // 执行 API 调用
            MvcResult testResult = mockMvc.perform(post("/api/jpa/insert")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andReturn();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            durations[i] = duration;

            // 获取结果
            String testResponseContent = testResult.getResponse().getContentAsString();
            Integer actualCount = Integer.valueOf(testResponseContent.trim());
            counts[i] = actualCount;

            System.out.println("  - 插入件数: " + actualCount);
            System.out.println("  - 执行时间: " + duration + "ms");
        }

        long totalTestEndTime = System.currentTimeMillis();

        System.out.println("✓ 正式测试完成");
        System.out.println();

        // ========== 第三步：统计结果 ==========
        System.out.println("========================================");
        System.out.println("测试结果统计");
        System.out.println("========================================");

        // 计算统计数据（基于3次正式测试）
        long totalDuration = durations[0] + durations[1] + durations[2];
        long totalRecords = counts[0] + counts[1] + counts[2];
        double avgDuration = totalDuration / 3.0;
        double minDuration = Math.min(durations[0], Math.min(durations[1], durations[2]));
        double maxDuration = Math.max(durations[0], Math.max(durations[1], durations[2]));
        double avgTimePerRecord = totalDuration / (double) totalRecords;
        double throughput = totalRecords * 1000.0 / totalDuration;

        // 验证结果
        for (int i = 0; i < 3; i++) {
            assertNotNull(counts[i], "返回结果不应为空");
            assertEquals(10000, counts[i], "插入件数应为10000");
        }

        // 输出统计信息
        System.out.println("✓ JPA 批量插入完成");
        System.out.println();
        System.out.println("性能统计（基于3次正式测试）:");
        System.out.println("========================================");
        System.out.println("1. 数据恢复阶段:");
        System.out.println("   - 恢复件数: " + restoredCount + " 条");
        System.out.println("   - 执行时间: " + restoreDuration + "ms");
        System.out.println();
        System.out.println("2. 数据插入阶段（3次测试汇总）:");
        System.out.println("   - 总插入件数: " + totalRecords + " 条");
        System.out.println("   - 总执行时间: " + totalDuration + "ms");
        System.out.println("   - 平均执行时间: " + String.format("%.2f", avgDuration) + "ms");
        System.out.println("   - 最小执行时间: " + minDuration + "ms");
        System.out.println("   - 最大执行时间: " + maxDuration + "ms");
        System.out.println();
        System.out.println("3. 性能指标:");
        System.out.println("   - 平均每条耗时: " + String.format("%.4f", avgTimePerRecord) + " ms/条");
        System.out.println("   - 吞吐量: " + String.format("%.2f", throughput) + " 条/秒");
        System.out.println();
        System.out.println("4. 总体统计:");
        System.out.println("   - 总耗时(含数据恢复): " + (restoreDuration + totalTestEndTime - totalTestStartTime) + "ms");
        System.out.println("========================================");
    }
}
