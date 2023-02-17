package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.configuration.CustomHttpHeaders;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.Cycles;
import org.anax.framework.reporting.model.Results;
import org.anax.framework.reporting.model.TestStepExecution;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.anax.framework.reporting.utilities.JsonUtilities.filterDataByAttributeValue;

@Service
@Slf4j
@ConditionalOnProperty(name = "zapi.instance", havingValue = "server")
public class ZephyrZAPIServerService implements ZephyrService {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    protected JiraService jiraService;

    @Autowired
    CustomHttpHeaders customHttpHeaders;

    @Value("${zapi.url:https:NOT_CONFIGURED}")
    private String zapiUrl;
    @Value("${jira.search.tc.attribute:label}")
    private String attribute;
    @Value("${zapi.status.pass.code:1}")
    private String pass;

    /**
     * Get cycle id from cycle name at UnSchedule
     *
     * @param projectName
     * @param cycleName
     * @return
     */
    @Override
    public String getCycleId(String projectName, String versionName, String cycleName, boolean initialSearch) {
        String projectId = jiraService.getProjectId(projectName);
        String versionId = jiraService.getVersionId(projectId, versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=" + versionId, HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Map.Entry<String, Map<Object, Object>> result = new Cycles(entity.getBody()).getContents().entrySet().stream().filter(x -> x.getValue().get("name").equals(cycleName)).findFirst().orElse(null);
        if (result == null)
            log.error("No Cycle found on project: {} with this name: {}", projectName, cycleName);
        return (result != null) ? result.getKey() : null;
    }

    /**
     * Get cycle id from cycle name at unschedule
     *
     * @param projectName
     * @param cycleName
     * @return
     */
    @Override
    public String getCycleIdUnderUnSchedule(String projectName, String cycleName) {
        String projectId = jiraService.getProjectId(projectName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=-1", HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Map.Entry<String, Map<Object, Object>> result = new Cycles(entity.getBody()).getContents().entrySet().stream().filter(x -> x.getValue().get("name").equals(cycleName)).findFirst().orElse(null);
        return (result != null) ? result.getKey() : null;
    }

    /**
     * Returns the test id from label or either the name - empty string in case no tc was found
     *
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param attributeValue
     * @throws Exception
     */
    @Override
    public String getIssueExecutionIdViaAttributeValue(String projectName, String versionName, String cycleName, String attributeValue, String cycleId) {
        String projectId = jiraService.getProjectId(projectName);
        String versionId = jiraService.getVersionId(projectId, versionName);

        try {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            return filterDataByAttributeValue((JSONArray) new JSONObject(restTemplate.exchange(zapiUrl + "execution?projectId=" + projectId + "&versionId=" + versionId + "&cycleId=" + cycleId, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), String.class).getBody()).get("executions"), attribute, attributeValue).get("id").toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Check !! Issue with this label: {} was not found on project: '{}' at version: '{}' and cycle: '{}'", attributeValue, projectName, versionName, cycleName);
            return "";
        }
    }

    @Override
    public String getIssueExecutionIssueIdViaAttributeValue(String projectName, String versionName, String cycleName, String attributeValue, String cycleId) {
        log.error("Not implemented for Zephyr server instance");
        return null;
    }

    @Override
    public String getIssueIdViaAttributeValue(String projectName, String versionName, String cycleName, String attributeValue, String cycleId) {
        log.error("Not implemented for Zephyr server instance");
        return null;
    }


    /**
     * Returns test step execution Id
     *
     * @param tcExecutionId
     * @param ordering
     * @return
     */
    @Override
    public String getTestStepExecutionId(String tcExecutionId, int ordering) {
        restTemplate.exchange(zapiUrl + "execution/" + tcExecutionId + "?expand=checksteps", HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), String.class);
        try {
            return getTestStepIdViaOrder(restTemplate.exchange(zapiUrl + "stepResult?executionId=" + tcExecutionId, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), new ParameterizedTypeReference<List<TestStepExecution>>() {
            }).getBody(), ordering);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("Test Step execution id do not found");
            return "";
        }
    }

    @Override
    public String getTestStepResultId(String executionId, String issueId, int stepOrder) {
        log.error("Not implemented for Zephyr server instance");
        return null;
    }

    /**
     * Get test steps executions
     *
     * @param tcExecutionId
     * @return
     */
    @Override
    public List getTestSteps(String tcExecutionId, String projectKey) {
        restTemplate.exchange(zapiUrl + "execution/" + tcExecutionId + "?expand=checksteps", HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), String.class);
        return restTemplate.exchange(zapiUrl + "stepResult?executionId=" + tcExecutionId, HttpMethod.GET, new HttpEntity<>(customHttpHeaders.getHeaders()), new ParameterizedTypeReference<List<TestStepExecution>>() {
        }).getBody();
    }

    @Override
    public String getTestStepId(String issueId, String projectId, int ordering) {
        log.error("Not implemented for Zephyr server instance");
        return null;
    }

    /**
     * Update test step status
     *
     * @param testStepExecutionId
     * @param status
     */
    @Override
    public void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod) {
        Map postBody = new HashMap();
        postBody.put("status", status);
        if (!status.equals(pass) && !StringUtils.isEmpty(testMethod.getDescription()))//not pass and has description
            postBody.put("comment", testMethod.getDescription());

        restTemplate.exchange(zapiUrl + "stepResult/" + testStepExecutionId, HttpMethod.PUT, new HttpEntity<>(postBody, customHttpHeaders.getHeaders()), String.class);
    }

    /**
     * Add attachement on execution
     *
     * @param executionId
     * @param file
     */
    @Override
    public void addTcExecutionAttachments(String executionId, File file) {
        LinkedMultiValueMap postBody = new LinkedMultiValueMap();
        postBody.add("file", new FileSystemResource(file));

        restTemplate.exchange(zapiUrl + "attachment?entityId=" + executionId + "&entityType=EXECUTION", HttpMethod.POST, new HttpEntity<>(postBody, getMultiPartHeaders()), String.class);
    }

    @Override
    public void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        log.error("Not implemented for Zephyr server instance!");
    }

    /**
     * Add attachment on execution step
     *
     * @param executionStepId
     * @param file
     */
    @Override
    public void addStepExecutionAttachments(String executionStepId, File file) {
        LinkedMultiValueMap postBody = new LinkedMultiValueMap();
        postBody.add("file", new FileSystemResource(file));

        restTemplate.exchange(zapiUrl + "attachment?entityId=" + executionStepId + "&entityType=stepResult", HttpMethod.POST, new HttpEntity<>(postBody, getMultiPartHeaders()), String.class);
    }

    @Override
    public void addStepExecutionAttachments(String stepResultId, String tcExecutionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        log.error("Not implemented for Zephyr server instance!");
    }

    /**
     * Clone a cycle (including executions) from default cycle to specific cycle
     *
     * @param projectName
     * @param versionName
     * @param cycleClone
     * @param originalCycleName
     */
    @Override
    public void cloneCycleToVersion(String projectName, String versionName, CycleClone cycleClone, String originalCycleName) {
        String projectId = jiraService.getProjectId(projectName);
        String versionId = jiraService.getVersionId(projectId, versionName);
        String cycleId = getCycleIdUnderUnSchedule(projectName, originalCycleName);

        cycleClone.setProjectId(projectId);
        cycleClone.setVersionId(versionId);
        cycleClone.setClonedCycleId(cycleId);
        restTemplate.exchange(zapiUrl + "cycle", HttpMethod.POST, new HttpEntity(cycleClone, customHttpHeaders.getHeaders()), CycleClone.class);
    }

    /**
     * Update execution results as bulk
     *
     * @param results
     */
    @Override
    public void updateBulkResults(Results results) {
        restTemplate.exchange(zapiUrl + "execution/updateBulkStatus/", HttpMethod.PUT, new HttpEntity(results, customHttpHeaders.getHeaders()), Results.class);
    }

    /**
     * Update test step status
     *
     * @param tcExecutionID
     * @param comment
     */
    @Override
    public void updateTestExecutionComment(String tcExecutionID, String comment) {
        Map postBody = new HashMap();
        postBody.put("comment", comment);

        restTemplate.exchange(zapiUrl + "execution/" + tcExecutionID + "/execute", HttpMethod.PUT, new HttpEntity<>(postBody, customHttpHeaders.getHeaders()), String.class);
    }

    @Override
    public void updateTestExecutionComment(String projectName, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment, String cycleId) {
        log.error("Not implemented for Zephyr server instance!");
    }


    /**
     * Update test execution bugs
     *
     * @param tcExecutionID
     * @param bugs
     */
    @Override
    public void updateTestExecutionBugs(String tcExecutionID, List<String> bugs) {
        Map postBody = new HashMap();
        postBody.put("defectList", bugs);
        postBody.put("updateDefectList", "true");

        restTemplate.exchange(zapiUrl + "execution/" + tcExecutionID + "/execute", HttpMethod.PUT, new HttpEntity<>(postBody, customHttpHeaders.getHeaders()), String.class);
    }

    @Override
    public void updateTestExecutionBugs(String projectName, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs, String cycleId) {
        log.error("Not implemented for Zephyr server instance!");
    }

    @Override
    public void updateTestStepStatus(String tcExecutionId, String stepResultId, String executionIssueId, String testStepId, String status) {
        log.error("Not implemented for Zephyr server instance");
    }


    //Search all the steps of tc executionId , returns the testStepId according the orderId
    private String getTestStepIdViaOrder(List<TestStepExecution> testStepExecutions, int ordering) {

        TestStepExecution result = testStepExecutions.stream().filter(it -> it.getOrderId().equals(ordering + 1)).findFirst().orElse(null);
        return (result != null) ? String.valueOf(result.getId()) : "";
    }

    private HttpHeaders getMultiPartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Atlassian-Token", "nocheck");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }
}
