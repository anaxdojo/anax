package org.anax.framework.configuration;

import org.anax.framework.reporting.AnaxAllureReporter;
import org.anax.framework.reporting.AnaxCombinedReporter;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.service.AnaxZapiReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AnaxCombinedReporterConfiguration {

    @Bean
    @Primary
    AnaxTestReporter anaxTestReporter(@Autowired @Qualifier("allureAnaxTestReporter") AnaxAllureReporter anaxAllureReporter, @Autowired @Qualifier("zapiAnaxTestReporter") AnaxZapiReporter anaxZapiReporter){
        return new AnaxCombinedReporter(anaxAllureReporter, anaxZapiReporter);
    }
}
