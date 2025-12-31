package com.zhaochuninhefei.orm.comparison.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 订单明细结果DTO
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResult {
    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 客户类型
     */
    private Integer customerType;

    /**
     * 地区名称
     */
    private String regionName;

    /**
     * 订单总金额
     */
    private Double totalAmount;

    /**
     * 实际金额
     */
    private Double actualAmount;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 明细数量
     */
    private Long detailCount;

    /**
     * 明细总金额
     */
    private Double detailTotalAmount;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类销售数量
     */
    private Long categorySalesCount;

    /**
     * 分类销售总额
     */
    private Double categoryTotalSales;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 创建时间
     */
    private String createTime;
}
