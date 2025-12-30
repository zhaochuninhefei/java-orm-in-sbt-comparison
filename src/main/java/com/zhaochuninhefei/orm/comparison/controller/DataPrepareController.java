package com.zhaochuninhefei.orm.comparison.controller;

import com.zhaochuninhefei.orm.comparison.dto.DataPrepareResponse;
import com.zhaochuninhefei.orm.comparison.service.DataPrepareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据准备Controller
 * 提供数据准备API
 */
@RestController
@RequestMapping("/api/data")
@SuppressWarnings({"NullableProblems", "unused"})
public class DataPrepareController {

    private final DataPrepareService dataPrepareService;

    public DataPrepareController(DataPrepareService dataPrepareService) {
        this.dataPrepareService = dataPrepareService;
    }

    /**
     * 准备测试数据
     *
     * @return 各张表的数据量
     */
    @PostMapping("/prepare")
    public ResponseEntity<DataPrepareResponse> prepareData() {
        DataPrepareResponse response = dataPrepareService.prepareAllData();
        return ResponseEntity.ok(response);
    }

    /**
     * 恢复user_profile表数据
     *
     * @return 恢复后的user_profile件数
     */
    @PostMapping("/jpa/restore/user_profile")
    public ResponseEntity<Integer> restoreUserProfileData() {
        int count = dataPrepareService.restoreUserProfileData();
        return ResponseEntity.ok(count);
    }
}