Design for java-orm-in-sbt-comparison
==========

# 背景
java-orm-in-sbt-comparison是一个用于比较java orm框架的springboot工程。

## 技术栈
- Java: OpenJDK 25
- SpringBoot: 4.0.1
- 数据库: MySQL 8
- JPA: 4.0.1
- Mybatis: 3.5.19

# 比较场景
准备比较以下场景中不同ORM框架的性能差异：

1. 单表插入 : 对一张数据量为10万的表进行单条数据插入,分别考虑单线程连续插入T条数据和N个并发线程分别插入M条数据的场景。
2. 批量插入 : 对一张数据量为10万的表进行批量插入，只考虑单线程分批插入10000条数据的场景。
3. 主键更新 ：对一张数据量为10万的表进行主键更新，只考虑N个并发线程分别更新M条数据的场景。
4. 条件更新 : 对一张数据量为10万的表进行条件更新，只考虑单线程条件更新10000条数据的场景。
5. 分页查询 : 基于一个复杂SQL(多表关联)进行分页查询,其中主表100万数据量，从表10万数据量，分页大小为100，关联5张表(两个内联,其他外联),有CTE查询,有嵌套子查询,有分组，有排序。分别考虑单线程分页查询和N个并发线程同时分页查询的场景。
6. 全表查询 : 对一个数据量为1000的表进行全表查询，分别考虑单线程全表查询和N个并发线程同时全表查询的场景。


# DB设计
参考 `designs/db/design-db.md`
- [DB设计](./db/design-db.md)

## 场景5：分页查询相关表

场景5涉及以下6张表：

| 表名 | 中文名 | 数据量 | 说明 |
|------|--------|--------|------|
| order_main | 订单主表 | 100万 | 主表，存储订单基本信息 |
| order_detail | 订单明细表 | 200万 | 从表，存储订单商品明细 |
| customer | 客户表 | 10万 | 客户基本信息 |
| product | 商品表 | 10万 | 商品基本信息 |
| product_category | 商品分类表 | 100 | 商品分类信息 |
| region | 地区表 | 1000 | 地区信息 |

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

# API设计

## a1.数据准备API
使用JPA实现数据准备API。

- uri: /api/data/prepare
- method: POST
- request body: null
- response body: 各张表的数据量
- 描述：基于`比较场景`与`DB设计`，为每张表生成对应的数据。(每张表的数据量，以及各个表之间的关系，参考`designs/db/design-db.md`文档)
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.DataPrepareController
- service: com.zhaochuninhefei.orm.comparison.service.DataPrepareService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity

## a2.恢复user_profile表数据API
使用JPA实现恢复user_profile表数据API。

- uri: /api/data/restore/user_profile
- method: POST
- request body: null
- response body: 恢复后的user_profile件数
- 描述：恢复user_profile表数据，默认恢复10万条数据。先truncate表，然后重新插入10万数据。
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.DataPrepareController
- service: com.zhaochuninhefei.orm.comparison.service.DataPrepareService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity


## b1.单表插入API(JPA)
使用JPA实现单表插入API，目标表是 user_profile

- uri: /api/jpa/insert
- method: POST
- request body: {insertCount: 1000}
- response body: 实际插入的件数
- 描述：根据传入的参数，插入指定数量的数据，默认1000。件数超过1000时, 分批插入。
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.JpaController
- service: com.zhaochuninhefei.orm.comparison.service.JpaService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity

## b2.主键更新API(JPA)
使用JPA实现主键更新API，目标表是 user_profile

- uri: /api/jpa/update/pk
- method: POST
- request body: null
- response body: 影响行数
- 描述：根据主键更新user_profile表中随机一行数据
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.JpaController
- service: com.zhaochuninhefei.orm.comparison.service.JpaService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity

## b3.条件更新API(JPA)
使用JPA实现条件更新API，目标表是 user_profile

- uri: /api/jpa/update/condition
- method: POST
- request body: {level: 5}
- response body: 影响行数
- 描述：条件更新user_profile表中指定level的数据，更新内容:age+1,salary+1000,description添加"update"到前面
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.JpaController
- service: com.zhaochuninhefei.orm.comparison.service.JpaService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity

## b4.分页查询API(JPA)
使用JPA实现分页查询API，具体的表和查询SQL参考`DB设计`的`场景5：分页查询相关表`。

- uri: /api/jpa/query/page
- method: POST
- request body: {pageNum: 1, pageSize: 100,s regionCode: 'REG00003', minActualPriceSum: 100}
- response body: 查询结果 + 分页信息
- 描述：分页查询，返回结果包含分页信息。具体的表和查询SQL参考`DB设计`的`场景5：分页查询相关表`。
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.JpaController
- service: com.zhaochuninhefei.orm.comparison.service.JpaService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity

## b5.全表查询API(JPA)
使用JPA实现全表查询API，目标表是 config_dict

- uri: /api/jpa/query/all
- method: POST
- request body: null
- response body: 查询结果
- 描述：全表查询, 目标表是 config_dict
- 技术栈: JPA + MySQL + SpringBoot

### 包与类设计
- controller: com.zhaochuninhefei.orm.comparison.controller.JpaController
- service: com.zhaochuninhefei.orm.comparison.service.JpaService
- repository: com.zhaochuninhefei.orm.comparison.jpa.repository
- entity: com.zhaochuninhefei.orm.comparison.jpa.entity
