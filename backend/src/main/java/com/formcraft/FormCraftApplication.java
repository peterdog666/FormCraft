package com.formcraft;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.formcraft.mapper")
public class FormCraftApplication {
    public static void main(String[] args) {
        SpringApplication.run(FormCraftApplication.class, args);
    }
}
