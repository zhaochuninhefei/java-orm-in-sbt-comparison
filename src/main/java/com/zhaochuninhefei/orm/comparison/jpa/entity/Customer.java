package com.zhaochuninhefei.orm.comparison.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户表
 * 对应表：customer
 * 数据量：10万
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "customer_no", nullable = false, unique = true, length = 50)
    private String customerNo;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "phone", unique = true, length = 20)
    private String phone;

    @Column(name = "customer_type")
    private Integer customerType = 1;

    @Column(name = "register_time", updatable = false)
    private LocalDateTime registerTime;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_consumption", precision = 15, scale = 2)
    private BigDecimal totalConsumption = BigDecimal.ZERO;

    @Column(name = "status")
    private Integer status = 1;

    @PrePersist
    protected void onCreate() {
        registerTime = LocalDateTime.now();
    }

}
