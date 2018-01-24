package org.anax.framework.configuration;


import org.anax.framework.controllers.ThreadAwareWebController;
import org.anax.framework.controllers.WebController;
import org.anax.framework.controllers.WebDriverWebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebControllerAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public WebController defaultWebController(@Autowired AnaxDriver anaxDriver, @Value("${anax.defaultWaitSeconds:5}") Integer defaultWaitSeconds) throws Exception {
        return new ThreadAwareWebController( () -> {
            try {
                return new WebDriverWebController(anaxDriver.getWebDriver(), defaultWaitSeconds);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(),e);
            }
        });

    }
}
