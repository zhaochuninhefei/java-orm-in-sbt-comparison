package com.zhaochuninhefei.orm.comparison.dto;

import lombok.Data;

/**
 * JPA 批量更新请求体
 */
@Data
public class BatchUpdateRequest {

    /**
     * 指定要更新的level
     * 默认值为5
     */
    private Integer level = 5;
}
