package com.zhaochuninhefei.orm.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 分页查询响应体
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageQueryResponse {

    /**
     * 查询结果列表
     */
    private List<OrderDetailResult> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

}
