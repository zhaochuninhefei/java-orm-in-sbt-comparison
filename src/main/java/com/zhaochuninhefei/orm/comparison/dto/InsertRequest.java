package com.zhaochuninhefei.orm.comparison.dto;

import lombok.Data;

/**
 * JPA插入请求DTO
 * 用于接收单表插入API的请求参数
 */
@Data
public class InsertRequest {
    private int insertCount = 1000; // 默认值为1000
}