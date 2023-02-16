package org.anax.framework.reporting.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.anax.framework.reporting.cache.DataObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@EnableRetry
public class RestTemplateConfig {

    @Value("${zapi.user:NOT_CONFIGURED}")
    private String user;
    @Value("${zapi.password:NOT_CONFIGURED}")
    private String password;
    @Value("${time_out:120000}")
    Duration time_out;

    @Bean("zapiRestTemplate")
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
                .basicAuthentication(user, password)
                .setConnectTimeout(time_out)
                .setReadTimeout(time_out)
                .build();
    }

    @Bean("cache")
    public @NonNull LoadingCache<String, DataObject> cache() {
        return Caffeine.newBuilder().build(DataObject::get);
    }
}
