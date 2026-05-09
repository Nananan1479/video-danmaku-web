package com.zsn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.mapper")
public class CiliCiliApplication {

    public static void main(String[] args) {
        SpringApplication.run(CiliCiliApplication.class, args);
    }
}
