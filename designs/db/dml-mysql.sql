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
 CALL generate_user_profiles(100000);

-- 2. 生成订单数据
 CALL generate_product_categories();        -- 100条分类
 CALL generate_products(100000);            -- 10万条商品
 CALL generate_customers(100000);          -- 10万条客户
 CALL generate_regions();                  -- 1000条地区
 CALL generate_order_mains(1000000);       -- 100万条订单
 CALL generate_order_details(2000000);     -- 200万条订单明细

-- 3. 生成配置字典数据（1000条）
 CALL generate_config_dicts();

-- ============================================
-- 验证数据生成情况
-- ============================================
 SELECT 'user_profile' AS table_name, COUNT(*) AS row_count FROM user_profile
 UNION ALL
 SELECT 'order_main', COUNT(*) FROM order_main
 UNION ALL
 SELECT 'order_detail', COUNT(*) FROM order_detail
 UNION ALL
 SELECT 'customer', COUNT(*) FROM customer
 UNION ALL
 SELECT 'product', COUNT(*) FROM product
 UNION ALL
 SELECT 'product_category', COUNT(*) FROM product_category
 UNION ALL
 SELECT 'region', COUNT(*) FROM region
 UNION ALL
 SELECT 'config_dict', COUNT(*) FROM config_dict;
