package com.tymbl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
    "com.tymbl.common.repository", 
    "com.tymbl.auth.repository", 
    "com.tymbl.interview.repository",
    "com.tymbl.jobs.repository", 
    "com.tymbl.registration.repository"
})
@EntityScan({
    "com.tymbl.common.entity", 
    "com.tymbl.auth.entity", 
    "com.tymbl.interview.entity",
    "com.tymbl.jobs.entity", 
    "com.tymbl.registration.entity"
})
public class JobReferralApplication {
    public static void main(String[] args) {
        SpringApplication.run(JobReferralApplication.class, args);
    }
} 