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
}
