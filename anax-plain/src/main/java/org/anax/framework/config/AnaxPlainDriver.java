package org.anax.framework.config;

import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.controllers.WebController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.anax.framework.controllers.VoidController;


@Configuration
public class AnaxPlainDriver {
    @ConditionalOnMissingBean
    @Bean
    public AnaxDriver defaultAnaxDriver() {
        return () -> null;
    }

    @Bean
    public WebController defaultWebController() {
        return new VoidController();
    }
}
