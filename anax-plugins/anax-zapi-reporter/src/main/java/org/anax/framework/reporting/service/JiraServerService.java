package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.configuration.CustomHttpHeaders;
import org.anax.framework.reporting.model.LabelValue;
import org.anax.framework.reporting.model.ProjectList;
import org.anax.framework.reporting.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "zapi.instance", havingValue = "server")
public class JiraServerService implements JiraService {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    protected AnaxZapiVersionResolver versionResolver;

    @Autowired
    CustomHttpHeaders customHttpHeaders;

    @Value("${jira.url:https:NOT_CONFIGURED}")
    private String jiraUrl;
    @Value("${zapi.url:https:NOT_CONFIGURED}")
    private String zapiUrl;

    /**
     * Get project id from project name
     *
     * @param projectName
     * @return
     */
    @Override
    public String getProjectId(String projectName) {
        ProjectList projectList = restTemplate.getForObject(zapiUrl + "util/project-list", ProjectList.class);
        LabelValue labelValue = projectList.getOptions().stream().filter(data -> data.getLabel().equals(projectName)).findFirst().orElse(null);
        if (labelValue == null)
            log.error("Check: No Project found with this name: {} , program will exit!!!", projectName);
        return (labelValue != null) ? labelValue.getValue() : "";
    }

    /**
     * Get version id from version name
     *
     * @param projectId
     * @param versionName
     * @return
     */
    @Override
    public String getVersionId(String projectId, String versionName) {
        ResponseEntity<List<Version>> versions = restTemplate.exchange(jiraUrl + "project/" + projectId + "/versions", HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), new ParameterizedTypeReference<List<Version>>() {
        });
        return versionResolver.getVersionFromJIRA(versionName, versions);
    }

    @Override
    public String getJiraIssueId(String issueKey) {
        log.error("Not implemented for Zephyr server instance");
        return null;
    }
}
