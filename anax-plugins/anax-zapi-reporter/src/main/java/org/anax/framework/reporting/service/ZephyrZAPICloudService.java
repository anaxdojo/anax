package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.authentication.JwtBuilder;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.CycleInfo;
import org.anax.framework.reporting.model.Results;
import org.anax.framework.reporting.model.Version;
import org.anax.framework.reporting.model.jira.Project;
import org.json.JSONArray;
import org.json.JSONException;
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
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Slf4j
@ConditionalOnProperty(name = "zapi.instance", havingValue = "cloud", matchIfMissing = true)
public class ZephyrZAPICloudService implements ZephyrService {
    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    protected AnaxZapiVersionResolver versionResolver;

    @Value("${zapi.url:https:NOT_CONFIGURED}")
    private String zapiUrl;
    @Value("${jira.url:https:NOT_CONFIGURED}")
    private String jiraUrl;
    @Value("${jira.search.tc.attribute:label}")
    private String attribute;
    @Value("${zapi.status.pass.code:1}")
    private String pass;
    @Value("${zapi.api.access.key:https:NOT_CONFIGURED}")
    private String zapiAccessKey;
    @Value("${zapi.api.secret.key:https:NOT_CONFIGURED}")
    private String zapiSecretKey;
    @Value("${jira.user.email:https:NOT_CONFIGURED}")
    private String jiraUserEmail;
    @Value("${jira.api.token:https:NOT_CONFIGURED}")
    private String jiraApiToken;

    /**
     * Get cycle id from cycle name at UnSchedule
     *
     * @param projectKey
     * @param cycleName
     * @return
     */
    @Override
    public String getCycleId(String projectKey, String versionName, String cycleName) {
        log.info(">>> Getting the cycle id for projectKey {}, versionName {}, cycleName {}", projectKey, versionName, cycleName);
        String projectId = getProjectId(projectKey);
        String versionId = "Unscheduled".equals(versionName) ? "-1" : getVersionId(projectKey, versionName);

        String requestUrl = zapiUrl + "/public/rest/api/1.0/cycles/search?projectId=" + projectId + "&versionId=" + versionId;
        String canonicalUrl = "GET&/public/rest/api/1.0/cycles/search&projectId=" + projectId + "&versionId=" + versionId;
        ResponseEntity<CycleInfo[]> cycleInfosArray = restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(getZapiHeaders(MediaType.TEXT_PLAIN, JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey))), CycleInfo[].class);
        CycleInfo cycleInfoFound = Arrays.stream(Objects.requireNonNull(cycleInfosArray.getBody())).filter(cycleInfo -> cycleInfo.getName().equals(cycleName)).findFirst().orElse(null);

        if (cycleInfoFound == null) {
            log.error("No Cycle found on project key: {} with this name: {} for the version: {}", projectKey, cycleName, versionName);
        } else {
            log.info("Cycle with id {} and name {} found on project key {} and version {}", cycleInfoFound.getId(), cycleName, projectKey, versionName);
        }
        return (cycleInfoFound != null) ? cycleInfoFound.getId() : null;
    }


    /**
     * Get project id from project name
     *
     * @param projectKey
     * @return
     */
    @Override
    public String getProjectId(String projectKey) {
        log.info(">>> Getting project id for projectKey {}", projectKey);
        String projectId = "";
        try {
            ResponseEntity<Project> projectResponseEntity = restTemplate.exchange(jiraUrl + "project/" + projectKey, HttpMethod.GET, new HttpEntity<>(getJiraHeaders()), Project.class);
            projectId = projectResponseEntity.getBody().getId();
        } catch (Exception e) {
            log.error("Error while getting the project id");
            e.printStackTrace();
        }
        log.info("Project id is: {}", projectId);
        return projectId;
    }

    /**
     * Get cycle id from cycle name at unschedule
     *
     * @param projectKey
     * @param cycleName
     * @return
     */
    @Override
    public String getCycleIdUnderUnSchedule(String projectKey, String cycleName) {
        return getCycleId(projectKey, "Unscheduled", cycleName);
    }


    /**
     * Get version id from version name
     *
     * @param projectKey
     * @param versionName
     * @return
     */
    @Override
    public String getVersionId(String projectKey, String versionName) {
        log.info(">>> Getting the version id for projectKey {}, versionName {}", projectKey, versionName);
        ResponseEntity<List<Version>> versions = restTemplate.exchange(jiraUrl + "project/" + projectKey + "/versions", HttpMethod.GET, new HttpEntity<>(getJiraHeaders()), new ParameterizedTypeReference<List<Version>>() {
        });
        log.info("Version id: {}", versionResolver.getVersionFromJIRA(versionName, versions));
        return versionResolver.getVersionFromJIRA(versionName, versions);
    }

    @Override
    public JSONObject getIssueExecutionViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue) {
        log.info(">>> getIssueExecutionIdViaAttributeValue for projectKey {}, versionName {}, cycleName {}, attributeValue {}", projectKey, versionName, cycleName, attributeValue);
        String projectId = getProjectId(projectKey);
        String versionId = getVersionId(projectKey, versionName);
        String cycleId = getCycleId(projectKey, versionName, cycleName);

        try {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

            String requestUrl = zapiUrl + "/public/rest/api/1.0/executions/search/cycle/" + cycleId + "?projectId=" + projectId + "&versionId=" + versionId;
            String canonicalUrl = "GET&/public/rest/api/1.0/executions/search/cycle/" + cycleId + "&projectId=" + projectId + "&versionId=" + versionId;
            return filterDataByAttributeValue((JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity(this.getZapiHeaders(MediaType.TEXT_PLAIN, JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey))), String.class).getBody()).get("searchObjectList"), attribute, attributeValue).getJSONObject("execution");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Check !! Issue with this label: {} was not found on project: '{}' at version: '{}' and cycle: '{}'", attributeValue, projectKey, versionName, cycleName);
            return null;
        }
    }

    /**
     * Returns the test id from label or either the name - empty string in case no tc was found
     *
     * @param projectKey
     * @param versionName
     * @param cycleName
     * @param attributeValue
     */
    @Override
    public String getIssueExecutionIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue) {
        try {
            return getIssueExecutionViaAttributeValue(projectKey, versionName, cycleName, attributeValue).get("id").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Returns the test ISSUE ID from label or either the name - empty string in case no tc was found
     *
     * @param projectKey
     * @param versionName
     * @param cycleName
     * @param attributeValue
     */
    @Override
    public String getIssueExecutionIssueIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue) {
        try {
            return getIssueExecutionViaAttributeValue(projectKey, versionName, cycleName, attributeValue).get("issueId").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
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
    public String getIssueIdViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue) {
        String projectId = getProjectId(projectKey);
        String versionId = getVersionId(projectKey, versionName);
        String cycleId = getCycleId(projectKey, versionName, cycleName);

        try {
            String requestUrl = zapiUrl + "/public/rest/api/2.0/executions/search/cycle/" + cycleId + "?projectId=" + projectId + "&versionId=" + versionId;
            String canonicalUrl = "GET&/public/rest/api/2.0/executions/search/cycle/" + cycleId + "&projectId=" + projectId + "&versionId=" + versionId;
            JSONArray result = (JSONArray) new JSONObject(new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(getZapiHeaders(MediaType.TEXT_PLAIN, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class).getBody()).get("searchResult").toString()).get("searchObjectList");
            JSONObject jsonObject = filterDataByAttributeValue(result, attribute, attributeValue);
            return new JSONObject(jsonObject.get("execution").toString()).get("issueId").toString();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Check !! Issue with this label: {} was not found on project: '{}' at version: '{}' and cycle: '{}'", attributeValue, projectKey, versionName, cycleName);
            return "";
        }
    }


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
        String canonicalUrl = "GET&/public/rest/api/2.0/teststep/" + issueId + "&projectId=" + projectId;
        try {
            JSONArray result = (JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(this.getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class).getBody().toString()).get("testSteps");
            JSONObject jsonObject = filterDataByIntegerAttributeValue(result, "orderId", ordering);
            testStepId = jsonObject.get("id").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testStepId;
    }

    /**
     * Get test steps executions
     *
     * @param tcExecutionIssueId
     * @return
     */
    @Override
    public List getTestSteps(String tcExecutionIssueId, String projectKey) {
        log.info(">>> getTestSteps for tcExecutionIssueId {}, projectKey {}", tcExecutionIssueId, projectKey);
        String projectId = getProjectId(projectKey);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/teststep/" + tcExecutionIssueId + "?projectId=" + projectId;
        String canonicalUrl = "GET&/public/rest/api/1.0/teststep/" + tcExecutionIssueId + "&projectId=" + projectId;
        List<String> testStepsIds = new ArrayList<>();
        try {
            JSONArray testSteps = new JSONArray(this.restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity(this.getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class).getBody());
            for (int i = 0; i < testSteps.length(); i++) {
                testStepsIds.add((String) testSteps.getJSONObject(i).get("id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return testStepsIds;
    }

    /**
     * Update test step status
     *
     * @param testStepExecutionId
     * @param status
     */
    @Override
    public void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    public void updateTestStepStatus(String executionId, String stepResultId, String issueId, String stepId, String status) {
        Map postBody = new HashMap();
        Map<String, String> idMap = new HashMap<>();
        idMap.put("id", status);
        postBody.put("status", idMap);
        postBody.put("issueId", issueId);
        postBody.put("stepId", stepId);
        postBody.put("executionId", executionId);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/stepresult/" + stepResultId;
        String canonicalUrl = "PUT&/public/rest/api/1.0/stepresult/" + stepResultId + "&";
        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity(postBody, this.getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class);
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
            String canonicalUrl = "GET&/public/rest/api/1.0/stepresult/search&executionId=" + executionId + "&isOrdered=true&issueId=" + issueId;
            JSONArray result = (JSONArray) new JSONObject(restTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(this.getZapiHeaders(MediaType.TEXT_PLAIN, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class).getBody()).get("stepResults");
            JSONObject response = filterDataByIntegerAttributeValue(result, "orderId", stepOrder);
            testStepResultId = response.get("id").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return testStepResultId;
    }

    /**
     * Add attachement on execution
     *
     * @param executionId
     * @param file
     */
    @Override
    public void addTcExecutionAttachments(String executionId, File file) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    @Override
    public void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        addAttachments(entityId, executionId, issueId, projectId, versionId, cycleId, "execution", file);
    }

    /**
     * Add attachment on execution step
     *
     * @param executionStepId
     * @param file
     */
    @Override
    public void addStepExecutionAttachments(String executionStepId, File file) {
        log.error("Not implemented for Zephyr cloud instance!");
    }

    @Override
    public void addStepExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file) {
        addAttachments(entityId, executionId, issueId, projectId, versionId, cycleId, "stepResult", file);
    }

    private void addAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, String entityName, File file) {
        log.info(">>> addStepExecutionAttachments with entityId {}, executionId {}, issueId {}, projectId {}, versionId {}, cycleId {}, file {}", entityId, executionId, issueId, projectId, versionId, cycleId, file);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/attachment?comment=auto_upload&cycleId=" + cycleId + "&entityId=" + entityId + "&entityName=" + entityName + "&executionId=" + executionId + "&issueId=" + issueId + "&projectId=" + projectId + "&versionId=" + versionId;
        String canonicalUrl = "POST&/public/rest/api/1.0/attachment&comment=auto_upload&cycleId=" + cycleId + "&entityId=" + entityId + "&entityName=" + entityName + "&executionId=" + executionId + "&issueId=" + issueId + "&projectId=" + projectId + "&versionId=" + versionId;
        LinkedMultiValueMap postBody = new LinkedMultiValueMap();
        postBody.add("file", new FileSystemResource(file));
        restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(postBody, this.getZapiHeaders(MediaType.MULTIPART_FORM_DATA, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class);
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
        log.info(">>> cloneCycleToVersion projectKey {}, versionName {}, cycleClone {}, originalCycleName {}", projectKey, versionName, cycleClone, originalCycleName);
        String projectId = getProjectId(projectKey);
        String versionId = getVersionId(projectKey, versionName);
        String cycleId = getCycleIdUnderUnSchedule(projectKey, originalCycleName);

        cycleClone.setProjectId(projectId);
        cycleClone.setVersionId(versionId);
        //cycleClone.setClonedCycleId(cycleId);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/cycle?clonedCycleId=" + cycleId;
        String canonicalUrl = "POST&/public/rest/api/1.0/cycle&clonedCycleId=" + cycleId;
        restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(cycleClone, getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey))), CycleClone.class);
    }

    /**
     * Update execution results as bulk
     *
     * @param results
     */
    @Override
    public void updateBulkResults(Results results) {
        log.info(">>> updateBulkResults results {}", results);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/executions";
        String canonicalUrl = "POST&/public/rest/api/1.0/executions&";
        this.restTemplate.exchange(requestUrl, HttpMethod.POST, new HttpEntity(results, this.getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, this.zapiAccessKey, this.zapiSecretKey))), String.class);
    }

    /**
     * Update test step status
     *
     * @param tcExecutionID
     * @param comment
     */
    @Override
    public void updateTestExecutionComment(String tcExecutionID, String comment) {
        log.info(">>> updateTestExecutionComment  tcExecutionID {}, comment {}", tcExecutionID, comment);
        log.error("Not implemented for Zephyr cloud instance!");
    }

    @Override
    public void updateTestExecutionComment(String projectKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment) {
        log.info(">>> updateTestExecutionComment  projectKey {}, versionName {}, cycleName {}, tcExecutionID {}, tcExecutionIssueID {}, comment {}", projectKey, versionName, cycleName, tcExecutionID, tcExecutionIssueID, comment);
        String projectId = getProjectId(projectKey);
        String versionId = getVersionId(projectKey, versionName);
        String cycleId = getCycleId(projectKey, versionName, cycleName);
        Map postBody = new HashMap();
        postBody.put("comment", comment);
        postBody.put("cycleId", cycleId);
        postBody.put("projectId", projectId);
        postBody.put("versionId", versionId);
        postBody.put("issueId", tcExecutionIssueID);
        postBody.put("id", tcExecutionID);
        String requestUrl = zapiUrl + "/public/rest/api/1.0/execution/" + tcExecutionID + "?issueId=" + tcExecutionIssueID + "&projectId=" + projectId;
        String canonicalUrl = "PUT&/public/rest/api/1.0/execution/" + tcExecutionID + "&issueId=" + tcExecutionIssueID + "&projectId=" + projectId;

        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(postBody, getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey))), String.class);
    }


    @Override
    public void updateTestExecutionBugs(String tcExecutionID, List<String> bugs) {
        log.info(">>> updateTestExecutionBugs  tcExecutionID {}, bugs {}", tcExecutionID, bugs);
        log.error("Not implemented for Zephyr cloud instance!");
    }

    /**
     * Update test execution bugs
     *
     * @param tcExecutionID
     * @param bugs
     */
    @Override
    public void updateTestExecutionBugs(String projectKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs) {
        log.info(">>> updateTestExecutionBugs  tcExecutionID {}, bugs {}", tcExecutionID, bugs);
        String projectId = getProjectId(projectKey);
        String versionId = getVersionId(projectKey, versionName);
        String cycleId = getCycleId(projectKey, versionName, cycleName);
        List<String> bugsIds = new ArrayList<>();
        for (String bugKey : bugs) {
            bugsIds.add(getJiraIssueId(bugKey));
        }
        //bugs.forEach(bugKey -> getJiraIssue(bugKey));
        Map postBody = new HashMap();
        postBody.put("cycleId", cycleId);
        postBody.put("projectId", projectId);
        postBody.put("versionId", versionId);
        postBody.put("issueId", tcExecutionIssueID);
        postBody.put("id", tcExecutionID);
        postBody.put("defects", bugsIds);
        //postBody.put("updateDefectList", "true");
        String requestUrl = zapiUrl + "/public/rest/api/1.0/execution/" + tcExecutionID + "?issueId=" + tcExecutionIssueID + "&projectId=" + projectId;
        String canonicalUrl = "PUT&/public/rest/api/1.0/execution/" + tcExecutionID + "&issueId=" + tcExecutionIssueID + "&projectId=" + projectId;
        restTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(postBody, getZapiHeaders(MediaType.APPLICATION_JSON, JwtBuilder.generateJWTToken(canonicalUrl, zapiAccessKey, zapiSecretKey))), String.class);
    }


    //Find the correct object in a jsonArray based on the value of an attribute,Create a list of label and then get the index of the JsonObject with this label
    private JSONObject filterDataByAttributeValue(JSONArray jsonArray, String attribute, String labelValue) throws JSONException {
        int index;
        ArrayList<String> values = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            values.add(jsonArray.getJSONObject(i).getString(attribute).toLowerCase());
        }
        index = IntStream.range(0, values.size()).filter(i -> values.get(i).contains(labelValue.toLowerCase())).findFirst().getAsInt();
        return jsonArray.getJSONObject(index);
    }

    //Find the correct object in a jsonArray based on the value of an attribute,Create a list of label and then get the index of the JsonObject with this label
    private JSONObject filterDataByIntegerAttributeValue(JSONArray jsonArray, String attribute, int labelValue) throws JSONException {
        int index;
        ArrayList<Integer> values = new ArrayList<>();

        IntStream.range(0, jsonArray.length()).forEach(i -> {
            try {
                values.add(jsonArray.getJSONObject(i).getInt(attribute));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        index = IntStream.range(0, values.size()).filter(i -> values.get(i).equals(labelValue)).findFirst().getAsInt();
        return new JSONObject(jsonArray.getJSONObject(index).toString());
    }

    private HttpHeaders getJiraHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Base64.getEncoder().encodeToString((jiraUserEmail + ":" + jiraApiToken).getBytes()));
        return headers;
    }

    private HttpHeaders getZapiHeaders(MediaType mediaType, String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Authorization", "JWT " + jwt);
        headers.add("zapiAccessKey", zapiAccessKey);
        return headers;
    }

    /**
     * Gets the id of a Jira ticket
     *
     * @param issueKey
     * @return
     */
    private String getJiraIssueId(String issueKey) {
        log.info(">>> getJiraIssue issueKey {}", issueKey);
        String issueId = "";
        try {
            ResponseEntity<String> jiraIssueResponseEntity = restTemplate.exchange(jiraUrl + "issue/" + issueKey, HttpMethod.GET, new HttpEntity<>(getJiraHeaders()), String.class);
            if (jiraIssueResponseEntity.getStatusCode() != HttpStatus.OK) {
                log.error("Jira issue {} not found! Will return empty issue id", issueKey);
            } else {
                issueId = (String) new JSONObject(jiraIssueResponseEntity.getBody()).get("id");
                log.info(">>> Found jira issue with id: {}", issueId);
            }
        } catch (Exception e) {
            log.error("Error while getting jira issue");
            e.printStackTrace();
        }
        return issueId;
    }
}
