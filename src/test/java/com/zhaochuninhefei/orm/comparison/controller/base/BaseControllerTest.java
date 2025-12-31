package com.zhaochuninhefei.orm.comparison.controller.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"unused", "java:S2187", "SameParameterValue"})
@SpringBootTest
@ActiveProfiles("test")
public class BaseControllerTest {
    @Autowired
    public WebApplicationContext webApplicationContext;

    /**
     * 测试结果统计类
     */
    public static class TestStatistics {
        public long[] durations;
        public int[] counts;
        public String operationName;
        public long totalTestStartTime;
        public long totalTestEndTime;

        public TestStatistics(int iterations, String operationName) {
            this.durations = new long[iterations];
            this.counts = new int[iterations];
            this.operationName = operationName;
        }

        public void setDuration(int index, long duration) {
            this.durations[index] = duration;
        }

        public void setCount(int index, int count) {
            this.counts[index] = count;
        }

        public long getTotalDuration() {
            long total = 0;
            for (long d : durations) {
                total += d;
            }
            return total;
        }

        public long getTotalRecords() {
            long total = 0;
            for (int c : counts) {
                total += c;
            }
            return total;
        }

        public double getAvgDuration() {
            return getTotalDuration() / (double) durations.length;
        }

        public double getMinDuration() {
            long min = durations[0];
            for (long d : durations) {
                if (d < min) min = d;
            }
            return min;
        }

        public double getMaxDuration() {
            long max = durations[0];
            for (long d : durations) {
                if (d > max) max = d;
            }
            return max;
        }

        public double getAvgTimePerRecord() {
            return getTotalDuration() / (double) getTotalRecords();
        }

        public double getThroughput() {
            return getTotalRecords() * 1000.0 / getTotalDuration();
        }

        public void printStatistics() {
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
    public MockMvc createMockMvc() {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * 恢复 user_profile 表数据
     *
     * @param mockMvc MockMvc 实例
     * @return 恢复的数据件数和耗时
     */
    public RestoreResult restoreUserData(MockMvc mockMvc) throws Exception {
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
    public record RestoreResult(int count, long duration) {
    }

    /**
     * 执行 JVM 热机
     *
     * @param mockMvc       MockMvc 实例
     * @param url           API URL
     * @param requestBody   请求体
     * @param operationName 操作名称（插入/更新）
     */
    public void performWarmup(MockMvc mockMvc, String url, String requestBody, String operationName) throws Exception {
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
    public TestStatistics performFormalTest(MockMvc mockMvc, String url, String requestBody,
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
    public TestStatistics performFormalTest(MockMvc mockMvc, String url, String requestBody,
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
}
