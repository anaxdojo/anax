package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.reporting.model.Version;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "zapi.instance", havingValue = "cloud", matchIfMissing = true)
public class JiraCloudService implements JiraService {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    protected AnaxZapiVersionResolver versionResolver;

    @Autowired
    @Qualifier("single")
    protected HttpHeaders jiraHttpHeaders;

    @Value("${jira.url:https:NOT_CONFIGURED}")
    protected String jiraUrl;

    /**
     * Get project id from project key
     *
     * @param projectKey - the jira project key (e.g. RON)
     * @return
     */
    @Override
    public String getProjectId(String projectKey) {
        String projectId = "";
        try {
            ResponseEntity<String> projectResponseEntity = restTemplate.exchange(jiraUrl + "project/" + projectKey, HttpMethod.GET, new HttpEntity<>(jiraHttpHeaders), String.class);
            projectId = new JSONObject(projectResponseEntity.getBody()).get("id").toString();
            if (!StringUtils.hasLength(projectId)) {
                throw new RetryException(MessageFormat.format("Error while getting the project id for projectKey {0}", projectKey));
            }
        } catch (Exception e) {
            log.error("Error while getting the project id");
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting the project id for projectKey {0}", projectKey));
        }
        log.info("Project id is: {}", projectId);
        return projectId;
    }

    /**
     * Get version id from version name
     *
     * @param projectKey  - the jira project key (e.g. RON)
     * @param versionName - the version name to get the id from
     * @return
     */
    @Override
    public String getVersionId(String projectKey, String versionName) {
        String versionId = "";
        try {
            ResponseEntity<List<Version>> versions = restTemplate.exchange(jiraUrl + "project/" + projectKey + "/versions", HttpMethod.GET, new HttpEntity<>(jiraHttpHeaders), new ParameterizedTypeReference<List<Version>>() {
            });
            versionId = versionResolver.getVersionFromJIRA(versionName, versions);
            log.info("Version id: {}", versionId);
            if (!StringUtils.hasLength(versionId)) {
                throw new RetryException(MessageFormat.format("Exception while getting the version id for projectKey {0} and versionName {1}", projectKey, versionName));
            }
        } catch (Exception e) {
            throw new RetryException(MessageFormat.format("Exception while getting the version id for projectKey {0} and versionName {1}", projectKey, versionName));
        }
        return versionId;
    }

    /**
     * Gets the id of a Jira ticket
     *
     * @param issueKey
     * @return
     */
    public String getJiraIssueId(String issueKey) {
        String issueId = "";
        try {
            ResponseEntity<String> jiraIssueResponseEntity = restTemplate.exchange(jiraUrl + "issue/" + issueKey, HttpMethod.GET, new HttpEntity<>(jiraHttpHeaders), String.class);
            if (jiraIssueResponseEntity.getStatusCode() != HttpStatus.OK) {
                log.error("Jira issue {} not found!", issueKey);
                throw new RetryException(MessageFormat.format("Error while getting jira issue id for issueKey {0}", issueKey));
            } else {
                issueId = (String) new JSONObject(jiraIssueResponseEntity.getBody()).get("id");
                log.info("Found jira issue id: {} for issue key: {}", issueId, issueKey);
            }
        } catch (Exception e) {
            log.error("Error while getting jira issue id with key: {}", issueKey);
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting jira issue id for issueKey {0}", issueKey));
        }
        return issueId;
    }
}
