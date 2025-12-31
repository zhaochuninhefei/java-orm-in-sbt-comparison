package com.zhaochuninhefei.orm.comparison;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zhaochuninhefei.orm.comparison.mybatis.dao")
public class JavaOrmInSbtComparisonApplication {

    private JavaOrmInSbtComparisonApplication() {}

	static void main(String[] args) {
		SpringApplication.run(JavaOrmInSbtComparisonApplication.class, args);
	}

}
