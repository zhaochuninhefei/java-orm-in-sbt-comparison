package com.zhaochuninhefei.orm.comparison.dto;

import lombok.Data;

import java.util.List;

/**
 * 分页查询请求体
 */
@Data
public class PageQueryRequest {

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 100;

    /**
     * 订单状态列表（可选）
     */
    private List<Integer> orderStatus;

    /**
     * 地区代码（可选）
     */
    private String regionCode;

    /**
     * 最小实际价格总和（可选，用于HAVING条件）
     */
    private Double minActualPriceSum;
}
