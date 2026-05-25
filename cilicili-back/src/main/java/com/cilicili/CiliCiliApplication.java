package com.cilicili;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cilicili.mapper")
public class CiliCiliApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiliCiliApplication.class, args);
    }
}
