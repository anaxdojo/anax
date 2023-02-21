package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.cache.Caches;
import org.anax.framework.reporting.configuration.CustomHttpHeaders;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.CycleInfo;
import org.anax.framework.reporting.model.Results;
import org.anax.framework.reporting.utilities.CycleEnvironmentResolver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.IntStream;

import static org.anax.framework.reporting.utilities.JsonUtilities.filterDataByAttributeValue;
import static org.anax.framework.reporting.utilities.JsonUtilities.filterDataByIntegerAttributeValue;

@Service
@Slf4j
@ConditionalOnProperty(name = "zapi.instance", havingValue = "cloud", matchIfMissing = true)
public class ZephyrZAPICloudService implements ZephyrService {
    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    protected JiraService jiraService;

    @Autowired
    protected CycleEnvironmentResolver cycleEnvironmentResolver;

    @Autowired
    protected CustomHttpHeaders customHttpHeaders;

    @Autowired
    protected CacheManager cacheManager;

    @Value("${zapi.url:https:NOT_CONFIGURED}")
    protected String zapiUrl;
    @Value("${jira.search.tc.attribute:label}")
    protected String attribute;
    @Value("${spring.profiles.active:#{null}}")
    protected String environment;

    /**
     * Get cycle id from cycle name at UnSchedule
     *
     * @param projectKey
     * @param cycleName
     * @return
     */
    @Override
    public String getCycleId(String projectKey, String versionName, String cycleName, boolean initialSearch) {
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = "Unscheduled".equals(versionName) ? "-1" : jiraService.getVersionId(projectKey, versionName);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/cycles/search?projectId=" + projectId + "&versionId=" + versionId;
        ResponseEntity<List<CycleInfo>> cycleInfos = restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getZapiHeaders(MediaType.TEXT_PLAIN, HttpMethod.GET, requestUrl, zapiUrl)), new ParameterizedTypeReference<List<CycleInfo>>() {});
        CycleInfo cycleInfoFound;
        if (StringUtils.hasLength(environment)) {
            cycleInfoFound = Objects.requireNonNull(cycleInfos.getBody()).stream().filter(cycleInfo -> cycleInfo.getName().equals(cycleName) && cycleEnvironmentResolver.isCycleEnvironmentSameWithRunEnvironment(versionName, cycleInfo, getEnvironment())).findFirst().orElse(null);
        } else {
            cycleInfoFound = Objects.requireNonNull(cycleInfos.getBody()).stream().filter(cycleInfo -> cycleInfo.getName().equals(cycleName)).findFirst().orElse(null);
        }

        if (cycleInfoFound == null && initialSearch) {
            log.info("No Cycle found on project key: {} with this name: {} for the version: {} and environment: {}", projectKey, cycleName, versionName, getEnvironment());
        } else if (cycleInfoFound == null) {
            log.error("No Cycle found on project key: {} with this name: {} for the version: {} and environment: {}", projectKey, cycleName, versionName, getEnvironment());
            throw new RetryException(MessageFormat.format("No Cycle found on project key: {0} with this name: {1} for the version: {2} and environment: {3}", projectKey, cycleName, versionName, getEnvironment()));
        } else {
            log.info("Cycle with id: {} and name: {} found on project key: {} for version: {} and environment: {}", cycleInfoFound.getId(), cycleName, projectKey, versionName, getEnvironment());
        }
        return (cycleInfoFound != null) ? cycleInfoFound.getId() : null;
    }

    /**
     * Get cycle id from cycle name at Unscheduled
     *
     * @param projectKey - the jira project key (e.g. RON)
     * @param cycleName  - the cycle name to get under the Unscheduled version
     * @return
     */
    @Override
    public String getCycleIdUnderUnSchedule(String projectKey, String cycleName) {
        return getCycleId(projectKey, "Unscheduled", cycleName, false);
    }

    /**
     * Returns the execution id of a TC, for the given project, version and cycle
     *
     * @param projectKey     - the jira project key (e.g. RON)
     * @param versionName    - the version name that the execution is
     * @param cycleName      - the cycle name that the TC is
     * @param attributeValue - the TC number for which to get the execution
     * @param cycleId        - The cycle id
     */
    @Override
    public String getIssueExecutionIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue, String cycleId) {
        String issueExecutionId = "";
        try {
            issueExecutionId = getIssueExecutionViaAttributeValue(projectKey, versionName, cycleName, attributeValue, cycleId).get("id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting issue execution id via attribute value, for projectKey {0}, versionName {1}, cycleName {2}, attributeValue {3}", projectKey, versionName, cycleName, attributeValue));
        }
        return issueExecutionId;
    }

    /**
     * Returns the execution issue id of a TC, for the given project, version and cycle
     *
     * @param projectKey     - the jira project key (e.g. RON)
     * @param versionName    - the version name that the execution is
     * @param cycleName      - the cycle name that the TC is
     * @param attributeValue - the TC number for which to get the execution
     * @param cycleId        - The cycle id
     */
    @Override
    public String getIssueExecutionIssueIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue, String cycleId) {
        String issueExecutionIssueId = "";
        try {
            issueExecutionIssueId = getIssueExecutionViaAttributeValue(projectKey, versionName, cycleName, attributeValue, cycleId).get("issueId").toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting issue execution issue id via attribute value, for projectKey {0}, versionName {1}, cycleName {2}, attributeValue {3}", projectKey, versionName, cycleName, attributeValue));
        }
        return issueExecutionIssueId;
    }

    /**
     * Returns the issue id from label or either the name - empty string in case no tc was found
     *
     * @param projectKey
     * @param versionName
     * @param cycleName
     * @param attributeValue
     * @return
     */
    @Override
    public String getIssueIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue, String cycleId) {
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = jiraService.getVersionId(projectKey, versionName);
        String issueId = "";
        try {
            String requestUrl = zapiUrl + "/public/rest/api/2.0/executions/search/cycle/" + cycleId + "?projectId=" + projectId + "&versionId=" + versionId;
            JSONArray result = (JSONArray) new JSONObject(new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getZapiHeaders(MediaType.TEXT_PLAIN, HttpMethod.GET, requestUrl, zapiUrl)), String.class).getBody()).get("searchResult").toString()).get("searchObjectList");
            JSONObject jsonObject = filterDataByAttributeValue(result, attribute, attributeValue);
            issueId = new JSONObject(jsonObject.get("execution").toString()).get("issueId").toString();
            if (!StringUtils.hasLength(issueId)) {
                throw new RetryException(MessageFormat.format("Error while getting issue id via attribute value, for projectKey {0}, versionName {1}, cycleName {2}, attributeValue {3}", projectKey, versionName, cycleName, attributeValue));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Check !! Issue with this label: {} was not found on project: '{}' at version: '{}' and cycle: '{}'", attributeValue, projectKey, versionName, cycleName);
            throw new RetryException(MessageFormat.format("Error while getting issue id via attribute value, for projectKey {0}, versionName {1}, cycleName {2}, attributeValue {3}", projectKey, versionName, cycleName, attributeValue));
        }
        return issueId;
    }


    /**
     * Not implemented for Zephyr cloud instance
     *
     * @param tcExecutionId
     * @param ordering
     * @return
     */
    @Override
    public String getTestStepExecutionId(String tcExecutionId, int ordering) {
        log.error("Not implemented for Zephyr cloud instance!");
        return null;
    }

    /**
     * Returns test step ID
     *
     * @param issueId
     * @param projectId
     * @param ordering
     * @return
     */
    @Override
    public String getTestStepId(String issueId, String projectId, int ordering) {
        String testStepId = "";
        String requestUrl = zapiUrl + "/public/rest/api/2.0/teststep/" + issueId + "?projectId=" + projectId;
        try {
            JSONArray result = (JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.GET, requestUrl, zapiUrl)), String.class).getBody().toString()).get("testSteps");
            JSONObject jsonObject = filterDataByIntegerAttributeValue(result, "orderId", ordering);
            testStepId = jsonObject.get("id").toString();
            if (!StringUtils.hasLength(testStepId)) {
                throw new RetryException(MessageFormat.format("Error while getting test step id for issueId {0}, projectId {1}, ordering {2}", issueId, projectId, ordering));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting test step id for issueId {0}, projectId {1}, ordering {2}", issueId, projectId, ordering));
        }
        return testStepId;
    }

    /**
     * Get the test steps executions ids
     *
     * @param tcExecutionIssueId
     * @param projectKey
     * @return
     */
    @Override
    public List getTestSteps(String tcExecutionIssueId, String projectKey) {
        String projectId = jiraService.getProjectId(projectKey);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/teststep/" + tcExecutionIssueId + "?projectId=" + projectId;
        List<String> testStepsIds = new ArrayList<>();
        try {
            JSONArray testSteps = new JSONArray(this.restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity(customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.GET, requestUrl, zapiUrl)), String.class).getBody());
            IntStream.range(0, testSteps.length()).forEach(i -> {
                try {
                    testStepsIds.add((String) testSteps.getJSONObject(i).get("id"));
                } catch (JSONException e) {
                    throw new RetryException(e.toString());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting test steps for tcExecutionIssueId {0}, projectKey {1}", tcExecutionIssueId, projectKey));
        }

        return testStepsIds;
    }

    /**
     * Not implemented for Zephyr cloud instance
     *
     * @param testStepExecutionId
     * @param status
     * @param testMethod
     */
    @Override
    public void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Update test step status
     *
     * @param executionId
     * @param stepResultId
     * @param issueId
     * @param stepId
     * @param status
     */
    public void updateTestStepStatus(String executionId, String stepResultId, String issueId, String stepId, String status) {
        Map postBody = new HashMap();
        Map<String, String> idMap = new HashMap<>();
        idMap.put("id", status);
        postBody.put("status", idMap);
        postBody.put("issueId", issueId);
        postBody.put("stepId", stepId);
        postBody.put("executionId", executionId);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/stepresult/" + stepResultId;
        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity(postBody, customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.PUT, requestUrl, zapiUrl)), String.class);
    }

    /**
     * Returns test step result Id
     *
     * @param executionId
     * @param issueId
     * @param stepOrder
     * @return
     */
    @Override
    public String getTestStepResultId(String executionId, String issueId, int stepOrder) {
        String testStepResultId = "";
        try {
            String requestUrl = zapiUrl + "/public/rest/api/1.0/stepresult/search?executionId=" + executionId + "&isOrdered=true&issueId=" + issueId;
            JSONArray result = (JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getZapiHeaders(MediaType.TEXT_PLAIN, HttpMethod.GET, requestUrl, zapiUrl)), String.class).getBody()).get("stepResults");
            JSONObject response = filterDataByIntegerAttributeValue(result, "orderId", stepOrder);
            testStepResultId = response.get("id").toString();
            if (!StringUtils.hasLength(testStepResultId)) {
                throw new RetryException(MessageFormat.format("Error while getting test steps result id for executionId {0}, issueId {1}, stepOrder {2}", executionId, issueId, stepOrder));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RetryException(MessageFormat.format("Error while getting test steps result id for executionId {0}, issueId {1}, stepOrder {2}", executionId, issueId, stepOrder));
        }
        return testStepResultId;
    }

    /**
     * Add attachment on execution
     *
     * @param executionId
     * @param file
     */
    @Override
    public void addTcExecutionAttachments(String executionId, File file) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Add attachment on execution
     *
     * @param entityId
     * @param executionId
     * @param issueId
     * @param projectId
     * @param versionId
     * @param cycleId
     * @param file
     */
    @Override
    public void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        addAttachments(entityId, executionId, issueId, projectId, versionId, cycleId, "execution", file);
    }

    /**
     * Not implemented for Zephyr cloud instance
     *
     * @param executionStepId
     * @param file
     */
    @Override
    public void addStepExecutionAttachments(String executionStepId, File file) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Add attachment on execution step
     *
     * @param entityId
     * @param executionId
     * @param issueId
     * @param projectId
     * @param versionId
     * @param cycleId
     * @param file
     */
    @Override
    public void addStepExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        addAttachments(entityId, executionId, issueId, projectId, versionId, cycleId, "stepResult", file);
    }

    /**
     * Clone a cycle (including executions) from default cycle to specific cycle
     *
     * @param projectKey
     * @param versionName
     * @param cycleClone
     * @param originalCycleName
     */
    @Override
    public void cloneCycleToVersion(String projectKey, String versionName, CycleClone cycleClone, String originalCycleName) {
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = jiraService.getVersionId(projectKey, versionName);
        String cycleId = getCycleIdUnderUnSchedule(projectKey, originalCycleName);

        if (cycleId != null) {
            cycleClone.setProjectId(projectId);
            cycleClone.setVersionId(versionId);
            if (StringUtils.hasLength(environment)) {
                cycleClone.setEnvironment(getEnvironment());
            }
            String requestUrl = zapiUrl + "/public/rest/api/1.0/cycle?clonedCycleId=" + cycleId;
            ResponseEntity<CycleInfo> response = restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(cycleClone, customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.POST, requestUrl, zapiUrl)), CycleInfo.class);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                log.error("Error while cloning cycle {} from projectKey {} versionName {} ", originalCycleName, projectKey, versionName);
                throw new RetryException(MessageFormat.format("Error while cloning cycle {0} from projectKey {1} versionName {2} ", originalCycleName, projectKey, versionName));
            } else {//Take created cycle id after clone - cache it
                Objects.requireNonNull(cacheManager.getCache(Caches.CYCLES)).put(projectKey + versionName + originalCycleName, response.getBody().getId());
            }

        } else {
            log.error("Cycle with name {} does not exist on 'Unschedule' of project: {}", originalCycleName, projectKey);
        }
    }

    /**
     * Update execution results as bulk
     *
     * @param results
     */
    @Override
    public void updateBulkResults(Results results) {
        String requestUrl = zapiUrl + "/public/rest/api/1.0/executions";
        this.restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(results, customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.POST, requestUrl, zapiUrl)), String.class);
    }

    /**
     * Update test step status
     *
     * @param tcExecutionID
     * @param comment
     */
    @Override
    public void updateTestExecutionComment(String tcExecutionID, String comment) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Update test step status
     *
     * @param projectKey
     * @param versionName
     * @param cycleName
     * @param tcExecutionID
     * @param tcExecutionIssueID
     * @param comment
     */
    @Override
    public void updateTestExecutionComment(String projectKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment, String cycleId) {
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = jiraService.getVersionId(projectKey, versionName);
        Map postBody = new HashMap();
        postBody.put("comment", comment);
        postBody.put("cycleId", cycleId);
        postBody.put("projectId", projectId);
        postBody.put("versionId", versionId);
        postBody.put("issueId", tcExecutionIssueID);
        postBody.put("id", tcExecutionID);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/execution/" + tcExecutionID + "?issueId=" + tcExecutionIssueID + "&projectId=" + projectId;

        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(postBody, customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.PUT, requestUrl, zapiUrl)), String.class);
    }


    /**
     * Not implemented for Zephyr cloud instance
     *
     * @param tcExecutionID
     * @param bugs
     */
    @Override
    public void updateTestExecutionBugs(String tcExecutionID, List<String> bugs) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Update test execution bugs
     *
     * @param projectKey
     * @param versionName
     * @param cycleName
     * @param tcExecutionID
     * @param tcExecutionIssueID
     * @param bugs
     */
    @Override
    public void updateTestExecutionBugs(String projectKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs, String cycleId) {
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = jiraService.getVersionId(projectKey, versionName);
        List<String> bugsIds = new ArrayList<>();
        bugs.forEach(bugKey -> bugsIds.add(jiraService.getJiraIssueId(bugKey)));
        Map postBody = new HashMap();
        postBody.put("cycleId", cycleId);
        postBody.put("projectId", projectId);
        postBody.put("versionId", versionId);
        postBody.put("issueId", tcExecutionIssueID);
        postBody.put("id", tcExecutionID);
        postBody.put("defects", bugsIds);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/execution/" + tcExecutionID + "?issueId=" + tcExecutionIssueID + "&projectId=" + projectId;
        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(postBody, customHttpHeaders.getZapiHeaders(MediaType.APPLICATION_JSON, HttpMethod.PUT, requestUrl, zapiUrl)), String.class);
    }

    /**
     * Get the execution of a TC, for the given project, version and cycle
     *
     * @param projectKey     - the jira project key (e.g. RON)
     * @param versionName    - the version name that the execution is
     * @param cycleName      - the cycle name that the TC is
     * @param attributeValue - the TC number for which to get the execution
     * @param cycleId        - The cycle id
     * @return
     */
    private JSONObject getIssueExecutionViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue, String cycleId) {
        JSONObject issueExecution;
        String projectId = jiraService.getProjectId(projectKey);
        String versionId = jiraService.getVersionId(projectKey, versionName);
        try {
            String requestUrl = zapiUrl + "/public/rest/api/1.0/executions/search/cycle/" + cycleId + "?projectId=" + projectId + "&versionId=" + versionId;
            issueExecution = filterDataByAttributeValue((JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity(customHttpHeaders.getZapiHeaders(MediaType.TEXT_PLAIN, HttpMethod.GET, requestUrl, zapiUrl)), String.class).getBody()).get("searchObjectList"), attribute, attributeValue).getJSONObject("execution");
            if (issueExecution == null) {
                throw new RetryException(MessageFormat.format("Check !! Issue with this label: {0} was not found on project: {1} at version: {2} and cycle: {3}", attributeValue, projectKey, versionName, cycleName));
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Check !! Issue with this label: {} was not found on project: '{}' at version: '{}' and cycle: '{}'", attributeValue, projectKey, versionName, cycleName);
            throw new RetryException(MessageFormat.format("Check !! Issue with this label: {0} was not found on project: {1} at version: {2} and cycle: {3}", attributeValue, projectKey, versionName, cycleName));
        }
        return issueExecution;
    }

    /**
     * Add attachment on an execution or test step
     *
     * @param entityId
     * @param executionId
     * @param issueId
     * @param projectId
     * @param versionId
     * @param cycleId
     * @param entityName
     * @param file
     */
    private void addAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, String entityName, File file) {
        String requestUrl = zapiUrl + "/public/rest/api/1.0/attachment?comment=auto_upload&cycleId=" + cycleId + "&entityId=" + entityId + "&entityName=" + entityName + "&executionId=" + executionId + "&issueId=" + issueId + "&projectId=" + projectId + "&versionId=" + versionId;
        LinkedMultiValueMap postBody = new LinkedMultiValueMap();
        postBody.add("file", new FileSystemResource(file));
        restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(postBody, customHttpHeaders.getZapiHeaders(MediaType.MULTIPART_FORM_DATA, HttpMethod.POST, requestUrl, zapiUrl)), String.class);
    }

    private String getEnvironment() {
        return environment == null ? null : environment.toLowerCase().trim();
    }
}
