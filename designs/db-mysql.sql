-- ============================================
-- Java ORM 比较测试 - MySQL 数据库脚本
-- ============================================

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

-- ============================================
-- 存储过程：生成测试数据
-- ============================================

DELIMITER //

-- 生成用户基础数据（10万条）
DROP PROCEDURE IF EXISTS generate_user_profiles//
CREATE PROCEDURE generate_user_profiles(IN count_num INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE departments VARCHAR(200) DEFAULT '研发部,产品部,市场部,销售部,人力资源部,财务部,运营部,客服部';
    DECLARE positions VARCHAR(200) DEFAULT '实习生,专员,主管,经理,总监,VP';

    WHILE i <= count_num DO
        INSERT INTO user_profile (
            username, email, phone, age, gender, status,
            department, position, salary, description, score, level
        ) VALUES (
            CONCAT('user_', LPAD(i, 6, '0')),
            CONCAT('user_', i, '@example.com'),
            CONCAT('1', LPAD(FLOOR(RAND() * 10000000000), 10, '0')),
            FLOOR(20 + RAND() * 40),
            FLOOR(1 + RAND() * 2),
            IF(RAND() > 0.1, 1, 0),
            SUBSTRING_INDEX(SUBSTRING_INDEX(departments, ',', FLOOR(1 + RAND() * 8)), ',', -1),
            SUBSTRING_INDEX(SUBSTRING_INDEX(positions, ',', FLOOR(1 + RAND() * 6)), ',', -1),
            ROUND(5000 + RAND() * 45000, 2),
            CONCAT('这是用户', i, '的描述信息'),
            ROUND(60 + RAND() * 40, 2),
            FLOOR(1 + RAND() * 10)
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成订单主表数据（100万条）
DROP PROCEDURE IF EXISTS generate_order_mains//
CREATE PROCEDURE generate_order_mains(IN count_num INT)
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= count_num DO
        INSERT INTO order_main (
            order_no, user_id, region_code, total_amount, discount_amount, actual_amount,
            order_status, payment_method, payment_time, ship_time, finish_time,
            receiver_name, receiver_phone, receiver_address, remark
        ) VALUES (
            CONCAT('ORD', DATE_FORMAT(NOW(), '%Y%m%d'), LPAD(i, 9, '0')),
            FLOOR(1 + RAND() * 100000),
            CONCAT('REG', LPAD(FLOOR(1 + RAND() * 1000), 5, '0')),
            ROUND(100 + RAND() * 10000, 2),
            ROUND(RAND() * 100, 2),
            ROUND(100 + RAND() * 9900, 2),
            FLOOR(RAND() * 5),
            FLOOR(1 + RAND() * 3),
            IF(RAND() > 0.3, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY), NULL),
            IF(RAND() > 0.4, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 360) DAY), NULL),
            IF(RAND() > 0.5, DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 355) DAY), NULL),
            CONCAT('收货人', i),
            CONCAT('1', LPAD(FLOOR(RAND() * 10000000000), 10, '0')),
            CONCAT('北京市朝阳区某街道', i, '号'),
            IF(RAND() > 0.7, CONCAT('备注信息', i), NULL)
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成订单明细数据（200万条）
DROP PROCEDURE IF EXISTS generate_order_details//
CREATE PROCEDURE generate_order_details(IN count_num INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE order_id BIGINT;
    DECLARE detail_index INT;

    -- 为每个订单生成2个明细（100万订单 x 2明细 = 200万明细）
    WHILE i <= 1000000 DO
        SET order_id = i;

        -- 为每个订单生成2个不同的商品明细
        SET detail_index = 1;
        WHILE detail_index <= 2 DO
            INSERT INTO order_detail (
                order_id, product_id, product_name, product_sku,
                quantity, unit_price, total_price, discount, actual_price
            ) VALUES (
                order_id,
                FLOOR(1 + RAND() * 100000),
                CONCAT('商品名称', order_id, '-', detail_index),
                CONCAT('SKU', LPAD(FLOOR(1 + RAND() * 100000), 6, '0')),
                FLOOR(1 + RAND() * 10),
                ROUND(50 + RAND() * 500, 2),
                ROUND((50 + RAND() * 500) * (1 + RAND() * 10), 2),
                ROUND(RAND() * 20, 2),
                ROUND((50 + RAND() * 500) * (1 + RAND() * 10) - RAND() * 20, 2)
            );
            SET detail_index = detail_index + 1;
        END WHILE;

        SET i = i + 1;
    END WHILE;
END//

-- 生成客户数据（10万条）
DROP PROCEDURE IF EXISTS generate_customers//
CREATE PROCEDURE generate_customers(IN count_num INT)
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= count_num DO
        INSERT INTO customer (
            customer_no, customer_name, email, phone, customer_type,
            last_login_time, total_orders, total_consumption, status
        ) VALUES (
            CONCAT('CUST', LPAD(i, 6, '0')),
            CONCAT('客户', i),
            CONCAT('customer', i, '@example.com'),
            CONCAT('1', LPAD(FLOOR(RAND() * 10000000000), 10, '0')),
            FLOOR(1 + RAND() * 3),
            DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY),
            FLOOR(RAND() * 100),
            ROUND(RAND() * 50000, 2),
            IF(RAND() > 0.1, 1, 0)
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成商品数据（10万条）
DROP PROCEDURE IF EXISTS generate_products//
CREATE PROCEDURE generate_products(IN count_num INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE brands VARCHAR(200) DEFAULT '品牌A,品牌B,品牌C,品牌D,品牌E';

    WHILE i <= count_num DO
        INSERT INTO product (
            product_no, product_name, product_sku, category_id, brand,
            price, cost_price, stock, sales, status
        ) VALUES (
            CONCAT('PROD', LPAD(i, 6, '0')),
            CONCAT('商品名称', i),
            CONCAT('SKU-', LPAD(i, 6, '0')),
            FLOOR(1 + RAND() * 100),
            SUBSTRING_INDEX(SUBSTRING_INDEX(brands, ',', FLOOR(1 + RAND() * 5)), ',', -1),
            ROUND(50 + RAND() * 500, 2),
            ROUND(30 + RAND() * 200, 2),
            FLOOR(RAND() * 1000),
            FLOOR(RAND() * 5000),
            IF(RAND() > 0.2, 1, 0)
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成商品分类数据（100条）
DROP PROCEDURE IF EXISTS generate_product_categories//
CREATE PROCEDURE generate_product_categories()
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= 100 DO
        INSERT INTO product_category (
            category_name, parent_id, level, sort_order, status
        ) VALUES (
            CONCAT('分类', i),
            IF(i > 10, FLOOR(1 + RAND() * 10), 0),
            IF(i <= 10, 1, 2),
            i,
            1
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成地区数据（1000条）
DROP PROCEDURE IF EXISTS generate_regions//
CREATE PROCEDURE generate_regions()
BEGIN
    DECLARE i INT DEFAULT 1;

    WHILE i <= 1000 DO
        INSERT INTO region (
            region_code, region_name, parent_id, level, sort_order, status
        ) VALUES (
            CONCAT('REG', LPAD(i, 5, '0')),
            CONCAT('地区', i),
            IF(i > 30, FLOOR(1 + RAND() * 30), 0),
            IF(i <= 30, 1, IF(i <= 300, 2, 3)),
            i,
            1
        );
        SET i = i + 1;
    END WHILE;
END//

-- 生成配置字典数据（1000条）
DROP PROCEDURE IF EXISTS generate_config_dicts//
CREATE PROCEDURE generate_config_dicts()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE types VARCHAR(200) DEFAULT 'system,business,user,product,order';

    WHILE i <= 1000 DO
        INSERT INTO config_dict (
            dict_code, dict_name, dict_value, dict_type, sort_order, status, remark
        ) VALUES (
            CONCAT('DICT_', LPAD(i, 4, '0')),
            CONCAT('字典项', i),
            CONCAT('VALUE_', i),
            SUBSTRING_INDEX(SUBSTRING_INDEX(types, ',', FLOOR(1 + RAND() * 5)), ',', -1),
            i,
            IF(RAND() > 0.1, 1, 0),
            CONCAT('这是字典项', i, '的说明')
        );
        SET i = i + 1;
    END WHILE;
END//

DELIMITER ;

-- ============================================
-- 执行数据生成
-- ============================================
-- 注意：根据实际需要执行以下存储过程

-- 1. 生成用户基础数据（10万条）
-- CALL generate_user_profiles(100000);

-- 2. 生成订单数据
-- CALL generate_product_categories();        -- 100条分类
-- CALL generate_products(100000);            -- 10万条商品
-- CALL generate_customers(100000);          -- 10万条客户
-- CALL generate_regions();                  -- 1000条地区
-- CALL generate_order_mains(1000000);       -- 100万条订单
-- CALL generate_order_details(2000000);     -- 200万条订单明细

-- 3. 生成配置字典数据（1000条）
-- CALL generate_config_dicts();

-- ============================================
-- 验证数据生成情况
-- ============================================
-- SELECT 'user_profile' AS table_name, COUNT(*) AS row_count FROM user_profile
-- UNION ALL
-- SELECT 'order_main', COUNT(*) FROM order_main
-- UNION ALL
-- SELECT 'order_detail', COUNT(*) FROM order_detail
-- UNION ALL
-- SELECT 'customer', COUNT(*) FROM customer
-- UNION ALL
-- SELECT 'product', COUNT(*) FROM product
-- UNION ALL
-- SELECT 'product_category', COUNT(*) FROM product_category
-- UNION ALL
-- SELECT 'region', COUNT(*) FROM region
-- UNION ALL
-- SELECT 'config_dict', COUNT(*) FROM config_dict;
