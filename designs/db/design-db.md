DB设计
==========

# SQL文件
- designs/db/ddl-mysql.sql
- designs/db/dml-mysql.sql

# DB说明

## 场景1-4：用户信息表
- table :  user_profile
- 数据量: 10万

参考: designs/db/ddl-mysql.sql 的 user_profile

## 场景6: 字典表
- table :  config_dict
- 数据量: 1000

参考: designs/db/ddl-mysql.sql 的 config_dict

## 场景5：分页查询 - ER关系设计
- table : order_main, order_detail, customer, product, product_category, region
- 数据量: order_main:100万, order_detail:200万, customer:10万, product:10万, product_category:100, region:1000

参考: designs/db/ddl-mysql.sql 的 order_main, order_detail, customer, product, product_category, region

### 表列表

场景5涉及以下6张表：

| 表名 | 中文名 | 数据量 | 说明 |
|------|--------|--------|------|
| order_main | 订单主表 | 100万 | 主表，存储订单基本信息 |
| order_detail | 订单明细表 | 200万 | 从表，存储订单商品明细 |
| customer | 客户表 | 10万 | 客户基本信息 |
| product | 商品表 | 10万 | 商品基本信息 |
| product_category | 商品分类表 | 100 | 商品分类信息 |
| region | 地区表 | 1000 | 地区信息 |

### ER关系图

```
customer (客户表)
    │ 1
    │
    │ N
order_main (订单主表) 1 ←─── N order_detail (订单明细表)
    │ N                          │
    │                            │ N
    │ 1                          │
    │                            │ 1
    ↓                            ↓
region (地区表)              product (商品表)
                                  │ N
                                  │
                                  │ 1
                                  ↓
                          product_category (商品分类表)
```

### 表关系详细说明

#### 1. customer ↔ order_main（一对多）
- **关系类型**：一对多
- **业务含义**：一个客户可以下多个订单
- **关联字段**：
    - `customer.id` → `order_main.user_id`
- **JOIN类型**：INNER JOIN（内联）
- **基数**：
    - 1个 customer : N个 order_main
    - 平均比例：1客户 : 10订单

#### 2. order_main ↔ order_detail（一对多）
- **关系类型**：一对多
- **业务含义**：一个订单包含多个订单明细
- **关联字段**：
    - `order_main.id` → `order_detail.order_id`
- **JOIN类型**：INNER JOIN（内联）
- **基数**：
    - 1个 order_main : N个 order_detail
    - 平均比例：1订单 : 2明细

#### 3. order_detail ↔ product（多对一）
- **关系类型**：多对一
- **业务含义**：多个订单明细可以对应同一商品
- **关联字段**：
    - `product.id` → `order_detail.product_id`
- **JOIN类型**：INNER JOIN（内联）
- **基数**：
    - N个 order_detail : 1个 product
    - 平均比例：20明细 : 1商品

#### 4. product ↔ product_category（多对一）
- **关系类型**：多对一
- **业务含义**：多个商品属于同一分类
- **关联字段**：
    - `product_category.id` → `product.category_id`
- **JOIN类型**：LEFT JOIN（外联）
- **基数**：
    - N个 product : 1个 product_category
    - 平均比例：1000商品 : 1分类

#### 5. order_main ↔ region（多对一）
- **关系类型**：多对一
- **业务含义**：订单所属地区
- **关联字段**：
    - `region.region_code` → `order_main.region_code`
- **JOIN类型**：LEFT JOIN（外联）
- **基数**：
    - N个 order_main : 1个 region
    - 说明：region_code允许NULL

### 关联字段汇总表

| 主表 | 从表 | 主表字段 | 从表字段 | 关系 |
|------|------|----------|----------|------|
| customer | order_main | customer.id | order_main.user_id | 1:N |
| order_main | order_detail | order_main.id | order_detail.order_id | 1:N |
| product | order_detail | product.id | order_detail.product_id | N:1 |
| product_category | product | product_category.id | product.category_id | 1:N |
| region | order_main | region.region_code | order_main.region_code | N:1 |

### 唯一性约束

为确保数据完整性和业务一致性，以下字段设置了唯一性约束：

**customer 表**：
- UNIQUE KEY: `uk_customer_no` (customer_no) - 客户编号唯一
- UNIQUE KEY: `uk_email` (email) - 邮箱唯一
- UNIQUE KEY: `uk_phone` (phone) - 手机号唯一

**order_main 表**：
- UNIQUE KEY: `uk_order_no` (order_no) - 订单编号唯一

**order_detail 表**：
- UNIQUE KEY: `uk_order_product` (order_id, product_id) - 同一订单同一商品唯一

**product 表**：
- UNIQUE KEY: `uk_product_no` (product_no) - 商品编号唯一
- UNIQUE KEY: `uk_product_sku` (product_sku) - 商品SKU唯一

**region 表**：
- UNIQUE KEY: `uk_region_code` (region_code) - 地区编码唯一

### 索引设计

为确保查询性能，以下字段已建立索引：

**order_main 表**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_order_no` (order_no) - 订单编号唯一
- INDEX: `idx_user_id` (user_id) - 用于关联customer
- INDEX: `idx_region_code` (region_code) - 用于关联region
- INDEX: `idx_create_time` (create_time) - **支持纯时间查询和排序**
- INDEX: `idx_status_create_time` (order_status, create_time) - **复合索引，优化状态+时间组合查询**
- INDEX: `idx_payment_time` (payment_time)

> **索引设计说明**：
> - `idx_create_time`：支持只有时间条件的查询（`WHERE create_time > ?`）
> - `idx_status_create_time`：支持状态+时间组合查询（`WHERE order_status = ? AND create_time > ?`）
> - 遵循最左前缀原则：复合索引 `(order_status, create_time)` 无法支持跳过 `order_status` 的查询
> - 两个索引互补，覆盖不同查询场景，虽增加存储但保证查询性能

**order_detail 表**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_order_product` (order_id, product_id) - 同一订单同一商品唯一
- INDEX: `idx_product_id` (product_id) - 用于关联product
- INDEX: `idx_product_sku` (product_sku) - 普通索引，支持按SKU查询

> **注**：虽然 DDL 中没有单独的 `idx_order_id` 索引，但由于存在复合唯一索引 `uk_order_product (order_id, product_id)`，根据最左前缀原则，查询 `WHERE order_id = ?` 可以使用这个复合索引的 `order_id` 部分。

**customer 表**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_customer_no` (customer_no) - 客户编号唯一
- UNIQUE KEY: `uk_email` (email) - 邮箱唯一
- UNIQUE KEY: `uk_phone` (phone) - 手机号唯一
- INDEX: `idx_register_time` (register_time)

> **注**：删除了低基数索引 `customer_type`（仅3个值：1-普通，2-VIP，3-SVIP）。如果需要按客户类型查询，建议与应用层过滤或与其他高选择性字段组合。

**product 表**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_product_no` (product_no) - 商品编号唯一
- UNIQUE KEY: `uk_product_sku` (product_sku) - 商品SKU唯一
- INDEX: `idx_category_id` (category_id) - 用于关联product_category

> **注**：删除了低基数索引 `status`（2个值）和 `brand`（5个品牌）。如果需要按品牌查询，建议应用层过滤或与 `category_id` 组合建复合索引。

**product_category 表**：
- PRIMARY KEY: `id`
- INDEX: `idx_parent_id` (parent_id)

> **注**：删除了低基数索引 `level`（2个值）和 `status`（2个值）。数据量仅100行，全表扫描更快。

**region 表**：
- PRIMARY KEY: `id`
- UNIQUE KEY: `uk_region_code` (region_code) - 地区编码唯一
- INDEX: `idx_parent_id` (parent_id)

> **注**：删除了低基数索引 `level`（仅3个值：1-省，2-市，3-区）。1000行数据，全表扫描比低效索引更快。

### 复杂查询示例

基于以上ER关系，场景5的复杂分页查询SQL示例：

```sql
-- CTE：统计各分类商品销售情况
WITH category_sales AS (
    SELECT
        pc.id AS category_id,
        pc.category_name,
        COUNT(od.id) AS sales_count,
        SUM(od.actual_price) AS total_sales
    FROM product_category pc
    INNER JOIN product p ON pc.id = p.category_id
    INNER JOIN order_detail od ON p.id = od.product_id
    INNER JOIN order_main om ON om.id = od.order_id
    WHERE om.region_code = 'REG00003'
    GROUP BY pc.id, pc.category_name
)
-- 主查询：多表关联分页查询
SELECT
    om.id AS order_id,
    om.order_no,
    c.customer_name,
    c.customer_type,
    r.region_name,
    om.total_amount,
    om.actual_amount,
    om.order_status,
    COUNT(od.id) AS detail_count,
    SUM(od.actual_price) AS detail_total_amount,
    p.product_name,
    pc.category_name,
    cs.sales_count AS category_sales_count,
    cs.total_sales AS category_total_sales,
    om.receiver_address,
    om.create_time
FROM order_main om
INNER JOIN customer c ON om.user_id = c.id
INNER JOIN order_detail od ON om.id = od.order_id
INNER JOIN product p ON od.product_id = p.id
LEFT JOIN product_category pc ON p.category_id = pc.id
LEFT JOIN region r ON om.region_code = r.region_code
LEFT JOIN category_sales cs ON pc.id = cs.category_id
WHERE om.region_code = 'REG00003'
GROUP BY om.id, om.order_no, c.customer_name, c.customer_type,
         om.total_amount, om.actual_amount, om.order_status,
         p.product_name, pc.category_name,
         cs.sales_count, cs.total_sales,
         om.receiver_address, om.create_time
HAVING SUM(od.actual_price) > 100
ORDER BY om.create_time DESC, om.id DESC
LIMIT 100 OFFSET 0
;
```

### 查询复杂度说明

该查询满足场景5的所有要求：

1. ✅ **多表关联**：关联6张表（customer, order_main, order_detail, product, product_category, region）
2. ✅ **两个内联**：customer, order_detail（使用INNER JOIN）
3. ✅ **其他外联**：product_category, region（使用LEFT JOIN）
4. ✅ **CTE查询**：使用WITH子句定义category_sales
5. ✅ **嵌套子查询**：CTE中包含聚合子查询
6. ✅ **分组**：使用GROUP BY进行分组统计
7. ✅ **排序**：使用ORDER BY多字段排序
8. ✅ **分页**：使用LIMIT + OFFSET实现分页（每页100条）

### 索引设计最佳实践

#### 低基数索引优化

**问题**：`order_status` 字段只有5个值（0-4），属于典型的低基数（Low Cardinality）字段。

**基数分析**：
```sql
SELECT
    COUNT(DISTINCT order_status) AS cardinality,
    COUNT(*) AS total_rows,
    COUNT(*) / COUNT(DISTINCT order_status) AS avg_rows_per_value
FROM order_main;
-- 结果：cardinality=5, total_rows=1000000, avg_rows_per_value=200000
```

**B-tree索引问题**：
- 低基数导致索引选择性差（每个值平均20万行）
- MySQL优化器倾向于全表扫描而非使用索引
- 索引维护开销大，收益小

**解决方案**：

1. **复合索引**（采用方案）✅
   ```sql
   CREATE INDEX idx_status_create_time ON order_main(order_status, create_time);
   ```
    - ✅ 利用 `create_time` 的高选择性
    - ✅ 支持索引排序（ORDER BY create_time）
    - ✅ 支持索引下推（Index Condition Pushdown）

2. **删除索引**
   ```sql
   -- 如果查询总是与其他高选择性字段组合，直接删除
   ALTER TABLE order_main DROP INDEX idx_order_status;
   ```

3. **分区表**（超大规模数据）
   ```sql
   PARTITION BY LIST COLUMNS(order_status) (
       PARTITION p_unpaid VALUES IN (0),
       PARTITION p_paid VALUES IN (1),
       ...
   );
   ```

**性能对比**：

| 索引类型 | 查询条件 | 索引使用 | 预估扫描行数 |
|---------|---------|---------|------------|
| 单列索引 | `WHERE order_status = 1` | ❌ 可能全表扫描 | ~100万 |
| 复合索引 | `WHERE order_status = 1 AND create_time > DATE_SUB(NOW(), INTERVAL 30 DAY)` | ✅ 使用索引 | ~2万 |
| 无索引 | `WHERE order_status = 1` | ❌ 全表扫描 | ~100万 |

#### 最左前缀原则与索引互补

**问题**：复合索引 `(order_status, create_time)` 能否替代单列索引 `create_time`？

**答案**：**不能**，因为最左前缀原则。

**复合索引查询场景分析**：

```sql
-- 索引：INDEX idx_status_create_time (order_status, create_time)

-- ✅ 场景1：使用完整索引
WHERE order_status = 1 AND create_time > '2024-01-01'
-- 使用索引：idx_status_create_time
-- type: ref, key: idx_status_create_time

-- ✅ 场景2：只使用最左列
WHERE order_status = 1
-- 使用索引：idx_status_create_time (只用第一列)
-- type: ref, key: idx_status_create_time

-- ❌ 场景3：跳过最左列（无法使用索引）
WHERE create_time > '2024-01-01'
-- 无法使用：idx_status_create_time（违反最左前缀原则）
-- 解决：需要单列索引 idx_create_time

-- ❌ 场景4：排序也无法使用索引
ORDER BY create_time DESC
-- 无法使用：idx_status_create_time（必须先有 order_status 条件）
```

**最左前缀原则总结**：

| 复合索引 | 可用查询场景 | 不可用查询场景 |
|---------|------------|--------------|
| `(A, B)` | `WHERE A = ?`<br>`WHERE A = ? AND B = ?`<br>`WHERE A = ? ORDER BY B` | `WHERE B = ?`<br>`ORDER BY B`<br>`WHERE B = ? ORDER BY A` |
| `(order_status, create_time)` | `WHERE order_status = ?`<br>`WHERE order_status = ? AND create_time > ?`<br>`WHERE order_status = ? ORDER BY create_time` | `WHERE create_time > ?`<br>`ORDER BY create_time` |

**最佳实践：双索引策略** ✅

```sql
-- 方案：同时保留两个索引，互补覆盖不同查询场景
CREATE INDEX idx_create_time ON order_main(create_time);
CREATE INDEX idx_status_create_time ON order_main(order_status, create_time);
```

**查询场景覆盖**：

| 查询类型 | 使用的索引 | 说明 |
|---------|-----------|------|
| `WHERE create_time > ?` | idx_create_time | ✅ 单列索引 |
| `WHERE create_time > ? ORDER BY create_time` | idx_create_time | ✅ 索引排序 |
| `WHERE order_status = ? AND create_time > ?` | idx_status_create_time | ✅ 复合索引 |
| `WHERE order_status = ?` | idx_status_create_time | ✅ 部分索引 |
| `WHERE order_status = ? ORDER BY create_time` | idx_status_create_time | ✅ 索引排序 |

**存储开销分析**：

```sql
-- 假设 order_main 表有100万行数据
-- BIGINT = 8字节，DATETIME = 8字节，TINYINT = 1字节

-- 单列索引：idx_create_time
-- 索引大小 ≈ 100万 × (8 + 8) = 16MB

-- 复合索引：idx_status_create_time
-- 索引大小 ≈ 100万 × (1 + 8) = 9MB

-- 总开销：25MB（可接受）
-- 收益：覆盖所有查询场景，避免全表扫描
```

**MySQL 8.0 索引跳跃扫描（Skip Scan）**：

MySQL 8.0 引入了索引跳跃扫描优化，**某些情况**下可以绕过最左前缀限制：

```sql
-- MySQL 8.13+ 可能支持
WHERE create_time > '2024-01-01'
-- 可能使用 idx_status_create_time（通过跳跃扫描）
-- 但性能不如直接的单列索引，不建议依赖
```

**结论**：
1. ✅ **保留单列索引** `idx_create_time`：覆盖纯时间查询
2. ✅ **保留复合索引** `idx_status_create_time`：覆盖状态+时间组合查询
3. ✅ **存储开销**：额外16MB，但查询性能大幅提升
4. ❌ **不推荐**：仅依赖复合索引，会导致大量查询全表扫描

**索引设计原则**：
1. ✅ **高选择性优先**：为高基数、高选择性字段建单列索引
2. ✅ **复合索引**：低基数 + 高基数字段组合
3. ✅ **覆盖索引**：包含查询所需所有字段，避免回表
4. ✅ **最左前缀**：考虑复合索引的最左前缀限制，必要时保留单列索引
5. ❌ **避免**：仅为低基数（值 < 10）字段建单列索引

#### 低基数索引识别与处理

**如何识别低基数索引？**

```sql
-- 计算字段的基数（唯一值数量）
SELECT
    COUNT(DISTINCT column_name) AS cardinality,
    COUNT(*) AS total_rows,
    COUNT(DISTINCT column_name) / COUNT(*) AS selectivity
FROM table_name;

-- 选择性评估：
-- selectivity > 0.9  → 高选择性，适合建索引
-- selectivity < 0.1  → 低选择性，不适合建单列索引
-- selectivity < 0.01 → 极低选择性，绝对不要建单列索引
```

**本项目中的低基数字段处理**：

| 表 | 字段 | 基数 | 数据量 | 选择性 | 处理方式 |
|---|------|------|--------|--------|---------|
| order_main | order_status | 5 | 100万 | 0.000005 | ✅ 复合索引 (order_status, create_time) |
| customer | customer_type | 3 | 10万 | 0.00003 | ❌ 删除单列索引 |
| product | status | 2 | 10万 | 0.00002 | ❌ 删除单列索引 |
| product | brand | 5 | 10万 | 0.00005 | ❌ 删除单列索引 |
| product_category | status | 2 | 100 | 0.02 | ❌ 删除（数据量小） |
| product_category | level | 2 | 100 | 0.02 | ❌ 删除（数据量小） |
| region | level | 3 | 1000 | 0.003 | ❌ 删除单列索引 |

**删除低效索引的原因**：

1. **product.status**（2个值）
   ```sql
   -- 之前的设计（低效）
   INDEX idx_status (status)

   -- 查询场景
   SELECT * FROM product WHERE status = 1;
   -- MySQL会选择全表扫描，因为：
   -- 1. 索引选择性极差（10万行 ÷ 2 = 5万行/值）
   -- 2. 回表成本高（需要读取5万次数据页）
   -- 3. 全表扫描可能更快（顺序IO）

   -- 如果确实需要查询status，建议复合索引
   CREATE INDEX idx_status_category ON product(status, category_id);
   -- 或者在应用层过滤（先查其他条件，再过滤status）
   ```

2. **customer.customer_type**（3个值）
   ```sql
   -- 之前的设计（低效）
   INDEX idx_customer_type (customer_type)

   -- 查询场景
   SELECT * FROM customer WHERE customer_type = 2;
   -- MySQL会全表扫描，因为：
   -- 1. 10万行 ÷ 3个类型 ≈ 3.3万行/值
   -- 2. 索引选择性 0.00003（极低）
   -- 3. 即使使用索引，回表成本也极高

   -- 推荐方案1：应用层过滤
   SELECT * FROM customer WHERE register_time > ?;
   -- 然后在应用中过滤 customer_type

   -- 推荐方案2：复合索引
   CREATE INDEX idx_type_register ON customer(customer_type, register_time);
   -- 但实际意义不大，因为 customer_type 分布通常很均匀
   ```

3. **product.brand**（5个值）
   ```sql
   -- 之前的设计（低效）
   INDEX idx_brand (brand)

   -- 品牌数据：仅5个品牌（品牌A,品牌B,品牌C,品牌D,品牌E）
   -- 查询场景
   SELECT * FROM product WHERE brand = '品牌A';
   -- MySQL会全表扫描，因为：
   -- 1. 10万行 ÷ 5个品牌 = 2万行/值
   -- 2. 索引选择性 0.00005（极低）
   -- 3. 回表成本高（2万次随机IO）

   -- 推荐方案：复合索引（如果确实需要按品牌查询）
   CREATE INDEX idx_category_brand ON product(category_id, brand);
   -- 查询示例：
   SELECT * FROM product WHERE category_id = 10 AND brand = '品牌A';
   -- 先用 category_id（100个分类，1000行/分类）过滤
   -- 再用 brand 进一步过滤到约400行
   -- 这样索引才有意义

   -- 或者在应用层过滤：
   SELECT * FROM product WHERE category_id = 10;
   -- 然后在应用中过滤 brand = '品牌A'
   ```

4. **product_category.status**（2个值）
   ```sql
   -- 数据量仅100行，全表扫描极快
   -- 100行 ÷ 8KB页 ≈ 1-2个数据页
   -- 索引查找需要：
   -- 1. 读取索引页
   -- 2. 随机IO读数据页
   -- 反而更慢！
   ```

5. **region.level**（3个值）
   ```sql
   -- 1000行数据，333行/值
   -- 选择性太差，MySQL优化器不会使用索引
   ```

**低基数索引的性能陷阱**：

```sql
-- 测试：product 表（10万行，status 2个值）

-- 场景1：单列索引（已删除）
CREATE INDEX idx_status ON product(status);
EXPLAIN SELECT * FROM product WHERE status = 1;
-- 结果：type=ALL（全表扫描）
-- 原因：MySQL评估后认为全表扫描更快

-- 场景2：复合索引（推荐）
CREATE INDEX idx_category_status ON product(category_id, status);
EXPLAIN SELECT * FROM product WHERE category_id = 10 AND status = 1;
-- 结果：type=ref（使用索引）
-- 原因：category_id 高选择性（100个分类，10万÷100=1000行/分类）

-- 场景3：无索引，全表扫描
EXPLAIN SELECT * FROM product WHERE status = 1;
-- 结果：type=ALL（全表扫描）
-- 性能：与使用单列索引相同，但节省了索引维护开销
```

**最佳实践总结**：

```sql
-- ✅ 推荐：高基数字段
INDEX idx_user_id (user_id)           -- 基数=10万
INDEX idx_email (email)               -- 基数=10万

-- ✅ 推荐：复合索引（低基数 + 高基数）
INDEX idx_status_time (order_status, create_time)  -- (5, 100万)
INDEX idx_category_status (category_id, status)    -- (100, 2)

-- ✅ 推荐：数据量小，无需索引
-- product_category 仅100行，region 仅1000行
-- 全表扫描只需1-2个IO，比索引查找更快

-- ❌ 避免：低基数单列索引
-- INDEX idx_status (status)          -- 基数=2
-- INDEX idx_level (level)            -- 基数=3
```

**验证索引有效性的方法**：

```sql
-- 1. 查看表的统计信息
SHOW TABLE STATUS LIKE 'product';

-- 2. 查看索引基数
SHOW INDEX FROM product;

-- 3. 分析索引选择性
SELECT
    COUNT(DISTINCT status) AS status_cardinality,
    COUNT(*) AS total_rows,
    ROUND(COUNT(DISTINCT status) / COUNT(*), 6) AS selectivity
FROM product;

-- 4. 执行计划分析
EXPLAIN SELECT * FROM product WHERE status = 1;
-- 关注：
-- - type: ALL（全表扫描）还是 ref（使用索引）
-- - rows: 预估扫描行数
-- - Extra: Using index condition（索引下推）

-- 5. 实际性能测试
SET profiling = 1;
SELECT * FROM product WHERE status = 1;
SHOW PROFILES;
```

**验证索引有效性**：
```sql
-- 查看执行计划
EXPLAIN SELECT * FROM order_main
WHERE order_status = 1 AND create_time > '2024-01-01'
ORDER BY create_time DESC;

-- 期望结果：
-- type: ref (使用索引)
-- key: idx_status_create_time
-- Extra: Using index condition
```
