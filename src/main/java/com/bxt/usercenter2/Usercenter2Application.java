package com.bxt.usercenter2;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.bxt.usercenter2.mapper")
@EnableScheduling
public class Usercenter2Application {

    public static void main(String[] args) {
        SpringApplication.run(Usercenter2Application.class, args);
    }

}
