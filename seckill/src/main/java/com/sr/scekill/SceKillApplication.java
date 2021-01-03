package com.sr.scekill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.sr.scekill.mapper")
public class SceKillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SceKillApplication.class);
    }
}
