package org.anax.framework.configuration;

import org.anax.framework.integrations.service.AnaxZapiVersionResolver;
import org.anax.framework.integrations.service.TestCaseToIssueResolver;
import org.anax.framework.integrations.service.impl.JiraZapiAppVersionResolver;
import org.anax.framework.integrations.service.impl.TestCaseToLabelResolver;
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

}
