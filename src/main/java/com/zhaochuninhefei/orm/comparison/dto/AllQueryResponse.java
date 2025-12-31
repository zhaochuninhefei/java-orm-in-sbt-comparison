package com.zhaochuninhefei.orm.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 全表查询响应体
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllQueryResponse<E> {

    /**
     * 查询结果列表
     */
    private List<E> records;

    /**
     * 总记录数
     */
    private Long total;
}
