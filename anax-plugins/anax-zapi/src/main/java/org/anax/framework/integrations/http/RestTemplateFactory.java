package org.anax.framework.integrations.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class RestTemplateFactory {
    @Value("${zapi.user}") private String user;
    @Value("${zapi.password}") private String password;
    @Value("${time_out:120000}") Duration time_out;

    @Bean
    RestTemplate restTemplateBuilder(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .basicAuthentication("user", "password")
                .setConnectTimeout(time_out)
                .setReadTimeout(time_out)
                .build();
    }
}
