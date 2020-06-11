package org.anax.framework.reporting.configuration;

import org.anax.framework.reporting.service.AnaxIssueAnnotationResolver;
import org.anax.framework.reporting.service.AnaxZapiVersionResolver;
import org.anax.framework.reporting.service.TestCaseToIssueResolver;
import org.anax.framework.reporting.IssueToBugAnnotationResolver;
import org.anax.framework.reporting.JiraZapiAppVersionResolver;
import org.anax.framework.reporting.TestCaseToLabelResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZapiConfig {

    @ConditionalOnMissingBean
    @Bean
    AnaxZapiVersionResolver versionResolver() {
        return new JiraZapiAppVersionResolver();
    }

    @ConditionalOnMissingBean
    @Bean
    TestCaseToIssueResolver issueResolver() {
        return new TestCaseToLabelResolver();
    }

    @ConditionalOnMissingBean
    @Bean
    AnaxIssueAnnotationResolver issueAnnotationResolver(){
        return new IssueToBugAnnotationResolver();
    }


}
