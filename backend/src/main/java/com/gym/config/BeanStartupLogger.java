package com.gym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "startup.diagnostics", name = "enabled", havingValue = "true")
public class BeanStartupLogger implements BeanPostProcessor {

    private final Set<String> loggedBeans = new HashSet<>();
    private final long startTime = System.currentTimeMillis();
    private final AtomicInteger beanCount = new AtomicInteger(0);

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String className = bean.getClass().getName();
        if (!loggedBeans.contains(className) && className.startsWith("com.gym")) {
            loggedBeans.add(className);
            int count = beanCount.incrementAndGet();
            log.info("[BEAN #{}] {} ({} ms)", count, beanName, System.currentTimeMillis() - startTime);
        }
        return bean;
    }
}
