java-orm-in-sbt-comparison
==========

# 工程介绍
这是一个用于比较java orm框架的springboot工程。

# 测试报告
- [JPA与Mybatis的单线程耗时对比结果](./reports/PerformanceCompare-jpa-mybatis-20251231.md)

# 技术栈
- Java: OpenJDK 25
- SpringBoot: 4.0.1
- 数据库: MySQL 8
- JPA: 4.0.1
- Mybatis: 3.5.19

# 编译与运行
该项目编译以及运行时需要指定Java为25，编译命令如下:
```sh
export JAVA_HOME=/usr/java/jdk-25.0.1+8
mvn -version # 检查JDK版本是不是25
mvn clean install package
```
也可以直接使用工程根目录下的脚本`mvn_build.sh`。

# 测试场景设计
- [测试场景设计](./designs/design-all.md)

# AI开发指南
部分代码基于AI编码工具开发，AI工具开发指南:
- [AI工具开发指南](./AGENT.md)
