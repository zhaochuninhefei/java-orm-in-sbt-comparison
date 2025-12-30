java-orm-in-sbt-comparison
==========

# 工程介绍
这是一个用于比较java orm框架的springboot工程。

# 技术栈
- Java: OpenJDK 25
- SpringBoot: 4.0.1
- 数据库: MySQL 8
- JPA: 4.0.1
- Mybatis: 3.5.19

# 运行
该项目运行时需要指定Java为25，具体命令如下:
```sh
export JAVA_HOME=/usr/java/jdk-25.0.1+8
mvn -version # 检查JDK版本是不是25
mvn clean install package
```
也可以直接使用工程根目录下的脚本`mvn_build.sh`。
