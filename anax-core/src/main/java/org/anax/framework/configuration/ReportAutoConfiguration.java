package org.anax.framework.configuration;


import org.anax.framework.controllers.WebController;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.DefaultJUnitReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:git.properties")
public class ReportAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AnaxTestReporter anaxTestReporter(@Value("${anax.reports.screenshot.dir:reports/screenshots}") String reportScreenshotDir,
            @Autowired Environment environment, @Autowired WebController controller) {
        return new DefaultJUnitReporter(environment, controller, reportScreenshotDir);
    }
}
