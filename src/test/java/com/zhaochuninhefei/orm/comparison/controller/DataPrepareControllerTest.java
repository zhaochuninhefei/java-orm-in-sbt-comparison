package com.zhaochuninhefei.orm.comparison.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("准备测试数据")
class DataPrepareControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    @DisplayName("准备测试数据")
    void testPrepareData() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MvcResult restoreResult = mockMvc.perform(post("/api/data/prepare"))
                .andExpect(status().isOk())
                .andReturn();

        // 打印响应结果
        System.out.println(restoreResult.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("恢复user_profile表数据")
    void testRestoreUserProfileData() throws Exception {
        // 设置 MockMvc
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MvcResult restoreResult = mockMvc.perform(post("/api/data/restore/user_profile"))
                .andExpect(status().isOk())
                .andReturn();

        // 打印响应结果
        System.out.println(restoreResult.getResponse().getContentAsString());
    }
}
