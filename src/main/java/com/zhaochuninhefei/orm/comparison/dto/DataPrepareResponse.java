package com.zhaochuninhefei.orm.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * 数据准备API响应DTO
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataPrepareResponse {

    private String message;
    private Map<String, Integer> tableCounts;
}
