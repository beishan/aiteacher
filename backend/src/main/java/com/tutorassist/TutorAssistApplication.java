package com.tutorassist;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tutorassist.**.mapper")
public class TutorAssistApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorAssistApplication.class, args);
    }
}
