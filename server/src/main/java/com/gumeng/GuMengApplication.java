package com.gumeng;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.gumeng.mapper")
public class GuMengApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuMengApplication.class, args);
    }
}
