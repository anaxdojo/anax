package org.anax.framework.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Value("${zapi.user:NOT_CONFIGURED_USER}") private String user;
    @Value("${zapi.password:NOT_CONFIGURED_PWD}") private String password;
    @Value("${time_out:120000}") Duration time_out;

    @Bean("zapiRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .basicAuthentication("user", "password")
                .setConnectTimeout(time_out)
                .setReadTimeout(time_out)
                .build();
    }
}
