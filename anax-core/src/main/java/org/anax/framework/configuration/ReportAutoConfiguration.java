package org.anax.framework.configuration;


import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.DefaultJUnitReporter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportAutoConfiguration {



    @Bean
    @ConditionalOnMissingBean
    AnaxTestReporter defaultJUnitTestReporter() {
        return new DefaultJUnitReporter();
    }
}
