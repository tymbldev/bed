package com.tymbl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tymbl")
@EnableAspectJAutoProxy
@EnableScheduling
public class TymblApplication {

  public static void main(String[] args) {
    SpringApplication.run(TymblApplication.class, args);
  }
} 