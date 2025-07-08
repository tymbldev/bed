package com.tymbl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = "com.tymbl")
@EnableAspectJAutoProxy
public class TymblApplication {

    public static void main(String[] args) {
        SpringApplication.run(TymblApplication.class, args);
    }
} 