PerformanceCompare-jpa-mybatis-20251231
==========

# 性能比较结果
在无并发压力(单线程请求)的情况下, JPA与Mybatis的性能基本持平, Mybatis稍好。
> 前提是JPA尽量采用标准的ORM的实现方式，操作entity而不是直接写SQL。

# 硬件规格
- CPU : 12th Gen Intel(R) Core(TM) i7-12700H
- 内存 : 32G
- 磁盘 : SSD 2T
- OS : Deepin 23.1

# 测试结果

## testInsertApiWith10000Records
插入1万条数据的耗时(目标表10万条数据)

| ORM | 平均耗时(ms) | 测试次数 |
| --- | --- | --- |
| jpa | 864.67 | 3 |
| mybatis | 399.67 | 3 |

> JPA采用的方案是 entity.save + entityManager.flush 分批插入, 并开启JDBC的SQL批量执行功能。 saveAll跑出来的性能不好。
> 
> Mybatis采用的方案是直接写批量插入SQL 分批插入。


## testUpdateByPk
主键更新的耗时(目标表10万条数据)

| ORM | 平均耗时(ms) | 测试次数 |
| --- | --- | --- |
| jpa | 4.33 | 3 |
| mybatis | 3.33 | 3 |


## testUpdateByCondition
条件更新(匹配到1万条数据)的耗时(目标表10万条数据)

| ORM | 平均耗时(ms) | 测试次数 |
| --- | --- | --- |
| jpa | 159.67 | 3 |
| mybatis | 156.33 | 3 |

> JPA也直接写自定义SQL实现条件更新

## testPageQuery
复杂分页查询的耗时(主表100万数据,从表200万数据)

| ORM | 平均耗时(ms) | 测试次数 |
| --- | --- | --- |
| jpa | 102.33 | 3 |
| mybatis | 99.67 | 3 |

> JPA直接自定义SQL + Pageable分页
> 
> Mybatis采用Mapper + PageHelper分页

## testQueryAll
全表查询的耗时(目标表1000条数据)

| ORM | 平均耗时(ms) | 测试次数 |
| --- | --- | --- |
| jpa | 19.00 | 3 |
| mybatis | 16.00 | 3 |

