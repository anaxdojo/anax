package org.anax.framework.configuration;


import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.DefaultJUnitReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ReportAutoConfiguration {



    @Bean
    @ConditionalOnMissingBean
    AnaxTestReporter defaultJUnitTestReporter(@Autowired Environment environment) {
        return new DefaultJUnitReporter(environment);
    }
}
