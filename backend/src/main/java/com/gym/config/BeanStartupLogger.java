package com.gym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class BeanStartupLogger implements BeanPostProcessor {

    private final Set<String> loggedBeans = new HashSet<>();
    private final long startTime = System.currentTimeMillis();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String className = bean.getClass().getName();
        if (!loggedBeans.contains(className) && (className.startsWith("com.gym") || className.contains("Stripe") || className.contains("Jwt"))) {
            loggedBeans.add(className);
            log.info("[BEAN] Created: {} (elapsed: {} ms)", beanName, elapsed());
        }
        return bean;
    }

    private long elapsed() {
        return System.currentTimeMillis() - startTime;
    }
}
