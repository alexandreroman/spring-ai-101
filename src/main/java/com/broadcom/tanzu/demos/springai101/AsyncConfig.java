package com.broadcom.tanzu.demos.springai101;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

@Configuration(proxyBeanMethods = false)
class AsyncConfig {
    @Bean
    public TaskDecorator taskDecorator() {
        // Enable context propagation when using async tasks.
        return new ContextPropagatingTaskDecorator();
    }
}
