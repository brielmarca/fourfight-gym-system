package com.gym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(exclude = RedisAutoConfiguration.class)
public class GymManagementApplication {

    public static void main(String[] args) {
        log.info("Starting Gym Management Application...");
        long start = System.currentTimeMillis();
        SpringApplication.run(GymManagementApplication.class, args);
        log.info("Application started in {} ms", System.currentTimeMillis() - start);
    }
}