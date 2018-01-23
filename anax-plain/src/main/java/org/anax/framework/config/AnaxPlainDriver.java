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
    public AnaxDriver getWebDriver() {
        return () -> null;
    }

    @ConditionalOnMissingBean
    @Bean
    public WebController getWebController() {
        return new VoidController();
    }
}
