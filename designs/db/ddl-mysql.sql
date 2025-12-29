-- 删除已存在的数据库（如果需要）
-- DROP DATABASE IF EXISTS orm_comparison_db;
-- CREATE DATABASE orm_comparison_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE orm_comparison_db;

-- ============================================
-- 1. 场景1-4的基础表（10万数据量）
-- 用于：单表插入、批量插入、主键更新、批量更新
-- ============================================
DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    age INT COMMENT '年龄',
    gender TINYINT COMMENT '性别：0-未知，1-男，2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    department VARCHAR(50) COMMENT '部门',
    position VARCHAR(50) COMMENT '职位',
    salary DECIMAL(10,2) COMMENT '薪资',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    description TEXT COMMENT '描述信息',
    score DECIMAL(5,2) COMMENT '评分',
    level INT COMMENT '等级',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_department (department),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基础信息表';

-- ============================================
-- 2. 场景5：分页查询表结构
-- ============================================

-- 2.1 订单主表（100万数据量）
DROP TABLE IF EXISTS order_main;
CREATE TABLE order_main (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    region_code VARCHAR(20) COMMENT '地区编码',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '优惠金额',
    actual_amount DECIMAL(12,2) NOT NULL COMMENT '实际支付金额',
    order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消',
    payment_method TINYINT COMMENT '支付方式：1-支付宝，2-微信，3-银行卡',
    payment_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    finish_time DATETIME COMMENT '完成时间',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    receiver_address VARCHAR(500) COMMENT '收货地址',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no) COMMENT '订单编号唯一',
    INDEX idx_user_id (user_id),
    INDEX idx_region_code (region_code),
    INDEX idx_create_time (create_time) COMMENT '时间索引，支持纯时间查询和排序',
    INDEX idx_status_create_time (order_status, create_time) COMMENT '状态+时间复合索引，优化低基数状态查询',
    INDEX idx_payment_time (payment_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单主表';

-- 2.2 订单明细表（200万数据量）
DROP TABLE IF EXISTS order_detail;
CREATE TABLE order_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_sku VARCHAR(50) NOT NULL COMMENT '商品SKU',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    total_price DECIMAL(12,2) NOT NULL COMMENT '小计金额',
    discount DECIMAL(5,2) DEFAULT 0.00 COMMENT '折扣',
    actual_price DECIMAL(12,2) NOT NULL COMMENT '实际价格',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_order_product (order_id, product_id) COMMENT '同一订单同一商品唯一',
    INDEX idx_product_id (product_id),
    INDEX idx_product_sku (product_sku)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 2.3 用户表（用于关联）
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '客户ID',
    customer_no VARCHAR(50) NOT NULL UNIQUE COMMENT '客户编号',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    customer_type TINYINT DEFAULT 1 COMMENT '客户类型：1-普通，2-VIP，3-SVIP',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    total_orders INT DEFAULT 0 COMMENT '总订单数',
    total_consumption DECIMAL(15,2) DEFAULT 0.00 COMMENT '总消费金额',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    UNIQUE KEY uk_customer_no (customer_no) COMMENT '客户编号唯一',
    UNIQUE KEY uk_email (email) COMMENT 'email唯一',
    UNIQUE KEY uk_phone (phone) COMMENT 'phone唯一',
    INDEX idx_register_time (register_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- 2.4 商品表（用于关联）
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    product_no VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编号',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_sku VARCHAR(50) UNIQUE COMMENT '商品SKU',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    brand VARCHAR(100) COMMENT '品牌',
    price DECIMAL(10,2) NOT NULL COMMENT '销售价格',
    cost_price DECIMAL(10,2) COMMENT '成本价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    sales INT DEFAULT 0 COMMENT '销量',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_product_no (product_no) COMMENT '商品编号唯一',
    UNIQUE KEY uk_product_sku (product_sku) COMMENT '商品SKU唯一',
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- 2.5 商品分类表（用于关联）
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    level TINYINT DEFAULT 1 COMMENT '层级',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- 2.6 地区表（用于关联）
DROP TABLE IF EXISTS region;
CREATE TABLE region (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地区ID',
    region_code VARCHAR(20) NOT NULL UNIQUE COMMENT '地区编码',
    region_name VARCHAR(100) NOT NULL COMMENT '地区名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父地区ID',
    level TINYINT DEFAULT 1 COMMENT '层级：1-省，2-市，3-区',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    UNIQUE KEY uk_region_code (region_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地区表';

-- ============================================
-- 3. 场景6：全表查询表（1000数据量）
-- ============================================
DROP TABLE IF EXISTS config_dict;
CREATE TABLE config_dict (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_code VARCHAR(50) NOT NULL UNIQUE COMMENT '字典编码',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_value VARCHAR(500) COMMENT '字典值',
    dict_type VARCHAR(50) COMMENT '字典类型',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置字典表';
