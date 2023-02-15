package org.anax.framework.reporting.configuration;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.authentication.JwtBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
@Slf4j
public class CustomHttpHeaders {

    @Value("${zapi.api.access.key:https:NOT_CONFIGURED}")
    private String zapiAccessKey;
    @Value("${zapi.api.secret.key:https:NOT_CONFIGURED}")
    private String zapiSecretKey;
    @Value("${jira.user.email:https:NOT_CONFIGURED}")
    private String jiraUserEmail;
    @Value("${jira.api.token:https:NOT_CONFIGURED}")
    private String jiraApiToken;

    @Bean(name = "single")
    public HttpHeaders getJiraHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(jiraUserEmail, jiraApiToken);
        return headers;
    }

    public HttpHeaders getZapiHeaders(MediaType mediaType, String canonicalUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Authorization", "JWT " + JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey));
        headers.add("zapiAccessKey", zapiAccessKey);
        return headers;
    }

}
