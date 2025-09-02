package com.tymbl.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfig {

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean("taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("TaskExecutor-");
    executor.setKeepAliveSeconds(300); // 5 minutes keep alive for idle threads
    executor.setAllowCoreThreadTimeOut(true); // Allow core threads to timeout
    executor.initialize();
    return executor;
  }
} 