# AGENT.md

该文件作为 AI-Coding-Tools (i.e. Claude-Code, Qoder, Lingma, or others) 的开发指南。
> 使用时请首先向工具声明使用本工具。例如: "请阅读`AGENT.md`文件并将其作为你的全局开发指南。"

## 交流语言
中文

## 项目概述

这是一个 Java ORM 框架性能对比项目,使用 Spring Boot 4.0.1 + Java 25 开发,主要对比不同 ORM 框架(目前包括 JPA 和 MyBatis)在各种场景下的性能表现。

## 项目构建和测试
```bash
# 每个mvn命令执行前都需要设置JDK版本
export JAVA_HOME=/usr/java/jdk-25.0.1+8
mvn -version # 检查JDK版本是不是25

# 清理并编译项目
mvn clean compile

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=JpaControllerTest

# 运行单个测试方法
mvn test -Dtest=JpaControllerTest#testInsertDataWithDefaultCount

# 打包项目(跳过测试)
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run
```

编译时可以直接使用脚本
```sh
./mvn_build.sh
```

### 数据准备
```bash
mvn test -Dtest=DataPrepareControllerTest#testPrepareData
```

## 项目配置
- 应用启动后访问 http://localhost:28080
- 默认数据库: MySQL on localhost:3307
- 数据库名: orm_comparison_db
- 项目配置: `src/main/resources/application.yaml`

## 架构设计

### 依赖管理

主要依赖:
- Spring Boot 4.0.1 ( starter-webmvc, starter-data-jpa )
- MyBatis Spring Boot Starter 4.0.0
- Lombok (注解处理器配置在 maven-compiler-plugin 中)
- MySQL Connector J


### 分层结构
项目采用标准的 Spring Boot 分层架构:
- **Controller 层**: `src/main/java/com/zhaochuninhefei/orm/comparison/controller/`
  - `DataPrepareController`: 数据准备相关 API (`/api/data/*`)
  - `JpaController`: JPA 性能测试 API (`/api/jpa/*`)

- **Service 层**: `src/main/java/com/zhaochuninhefei/orm/comparison/service/`
  - `DataPrepareService`: 负责生成和管理测试数据
  - `JpaService`: JPA 相关的业务逻辑实现

- **Entity 层**: `src/main/java/com/zhaochuninhefei/orm/comparison/jpa/entity/`
  - JPA 实体类定义,使用 Jakarta Persistence API

- **Repository 层**: `src/main/java/com/zhaochuninhefei/orm/comparison/jpa/repository/`
  - Spring Data JPA Repository 接口

- **DTO 层**: `src/main/java/com/zhaochuninhefei/orm/comparison/dto/`
  - 数据传输对象

### JPA批处理优化
项目的JPA部分使用了 Hibernate 批处理优化来提高批量插入性能:
- 批处理大小配置: `spring.jpa.hibernate.jdbc.batch_size=100` (可在 application.yaml 中调整)
- Service 层使用 `EntityManager.flush()` 和 `EntityManager.clear()` 来控制批处理
- 每达到 batch_size 数量时执行 flush 和 clear,避免内存堆积

### MyBatis 集成
项目引入了 MyBatis,相关实现包的位置:
- `src/main/java/com/zhaochuninhefei/orm/comparison/mybatis/mapper/*.xml`: MyBatis XML 映射文件
- `src/main/java/com/zhaochuninhefei/orm/comparison/mybatis/dao/*.java`: MyBatis Mapper 接口
- `src/main/java/com/zhaochuninhefei/orm/comparison/mybatis/po/*.java`: MyBatis SQL 结果集映射类
- `src/main/java/com/zhaochuninhefei/orm/comparison/controller/MybatisController.java`: MyBatis 测试 API


## 项目设计
项目设计文档位于目录`designs`下。

### 数据表设计
项目包含以下核心数据表:
- `user_profile`: 用户基础信息表(10万条) - 用于单表插入、批量插入、主键更新、批量更新场景
- `customer`: 客户表(10万条)
- `product`: 商品表(10万条)
- `product_category`: 商品分类表(100条)
- `region`: 地区表(1000条)
- `config_dict`: 配置字典表(1000条)
- `order_main`: 订单主表(100万条)
- `order_detail`: 订单明细表(200万条)


## 测试说明

### 数据准备 API
- `POST /api/data/prepare`: 准备所有测试数据(会先 TRUNCATE 所有表,然后插入数据)
- `POST /api/data/restore/user_profile`: 恢复 user_profile 表数据

### 测试类位置
- `src/test/java/com/zhaochuninhefei/orm/comparison/controller/JpaControllerTest.java`: JPA Controller 单元测试

### 测试注意事项
- 测试使用 `@SpringBootTest` 和 `MockMvc`
- 数据库连接配置需要正确才能运行测试
- 测试会向数据库插入实际数据,测试完成后需要手动清理

### 测试范例
- `POST /api/jpa/insert`: 单表插入 API,请求体示例: `{"insertCount": 1000}`
    - 单次插入限制最多 10000 条
    - 默认插入 1000 条


## 开发建议

1. 添加新的测试用例时,遵循现有的代码结构
2. 使用批处理优化时注意调整 batch_size 参数以获得最佳性能
3. 所有写入操作都应考虑使用事务注解 `@Transactional`
4. 测试数据生成建议使用批量插入以提高效率
