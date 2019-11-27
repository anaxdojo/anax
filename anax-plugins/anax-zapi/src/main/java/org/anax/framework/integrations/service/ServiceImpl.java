package org.anax.framework.integrations.service;

import org.anax.framework.integrations.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ServiceImpl {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Value("${zapi.url:http://jira.ath.persado.com:8080/rest/zapi/1.0/}") private String zapiUrl;
    @Value("${jira.url:http://jira.ath.persado.com:8080/rest/api/2/}") private String jiraUrl;
    @Value("${jira.originalCycle:Unresolved_Unplanned}") private String originalCycle;

    /**
     * Get project id from project name
     * @param projectName
     * @return
     */
    public String getProjectId(String projectName){
         ProjectList projectList = restTemplate.getForObject(zapiUrl +"util/project-list", ProjectList.class);
         return projectList.getOptions().stream().filter(data->data.getLabel().equals(projectName)).findFirst().get().getValue();
    }

    /**
     * Get version id from version name
     * @param projectId
     * @param versionName
     * @return
     */
    public String getVersionId(String projectId, String versionName){
        VersionList versionList = restTemplate.getForObject(zapiUrl +"util/versionBoard-list?projectId="+projectId, VersionList.class);
        return versionList.getOptions().stream().filter(data->data.getLabel().equals(versionName)).findFirst().get().getValue();
    }

    /**
     * Get cycle id from cycle name
     * @param projectName
     * @param versionName
     * @param cycleName
     * @return
     */
    public String getCycleId(String projectName,String versionName,String cycleName){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=" + versionId, HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Cycles cycle = new Cycles(entity.getBody());
        return  cycle.getCycleId(cycleName);
    }

    /**
     * Get issue id from issue name
     * @param issueName
     * @return
     */
    public String getIssueId(String issueName){
        return restTemplate.getForObject(jiraUrl+"issue/"+issueName, Issue.class).getId();
    }

    /**
     * Get cycle build number
     * @param projectName
     * @param versionName
     * @param cycleName
     * @return
     * @throws NoSuchFieldException
     */
    public String getCycleBuildNumber(String projectName,String versionName,String cycleName) throws NoSuchFieldException {
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=" + versionId, HttpMethod.GET,HttpEntity.EMPTY, Map.class);
        Cycles cycle = new Cycles((Map<String, Object>) entity.getBody());
        return  cycle.getCycleBuild(cycleName);
    }

    /**
     * Get the name of the latest cycle
     * @param projectName
     * @param versionName
     * @return
     * @throws NoSuchFieldException
     */
    public String getLatestCycleName(String projectName,String versionName) throws NoSuchFieldException {
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=" + versionId, HttpMethod.GET,HttpEntity.EMPTY, Map.class);
        Cycles cycle = new Cycles((Map<String, Object>) entity.getBody());
        return  cycle.getCycleNames().get(cycle.getCycleNames().size()-1);
    }

    /**
     * Get execution id from issue name
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param issueName
     * @return
     */
    public String getExecutionId(String projectName,String versionName,String cycleName, String issueName) {
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId, versionName);
        String cycleId = getCycleId(projectName, versionName, cycleName);
        String issueId = getIssueId(issueName);
        ExecutionList execution = restTemplate.getForObject(zapiUrl + "execution?issueId=" + issueId, ExecutionList.class);
        return execution.getExecutions().stream().filter(data -> data.getProjectId().equals(projectId) && data.getVersionId().equals(versionId) && data.getCycleId().equals(cycleId) && data.getIssueId().equals(issueId)).findFirst().get().getId();
    }

    /**
     * Update execution results as bulk
     * @param results
     */
    public void updateResults(Results results){
        restTemplate.exchange(zapiUrl+"execution/updateBulkStatus/",HttpMethod.PUT ,new HttpEntity(results, getHeaders()), Results.class);
    }

    /**
     * Update cycle info
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param cycleInfo
     */
    public void updateCycleInfo(String projectName,String versionName,String cycleName,CycleInfo cycleInfo) throws NoSuchFieldException {
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleId(projectName,versionName,cycleName);

        cycleInfo.setProjectId(projectId);
        cycleInfo.setVersionId(versionId);
        cycleInfo.setId(cycleId);
        restTemplate.exchange(zapiUrl+"cycle",HttpMethod.PUT ,new HttpEntity(cycleInfo, getHeaders()), CycleInfo.class);
    }

    /**
     * Clone a cyle (including executions) from default cycle to specific cycle
     * @param projectName
     * @param versionName
     * @param cycleClone
     * @param originalCycleName
     */
    public void cloneCycleToVersion(String projectName,String versionName,CycleClone cycleClone,String originalCycleName) throws NoSuchFieldException {
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleId(projectName,originalCycle,originalCycleName);

        cycleClone.setProjectId(projectId);
        cycleClone.setVersionId(versionId);
        cycleClone.setClonedCycleId(cycleId);
        restTemplate.exchange(zapiUrl+"cycle",HttpMethod.POST ,new HttpEntity(cycleClone, getHeaders()), CycleClone.class);
    }

    /**
     * Get all cycles name of a specific version
     * @param projectName
     * @param versionName
     * @return
     */
    public List<String> getVersionCycleNames(String projectName, String versionName){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId + "&versionId=" + versionId, HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Cycles cycle = new Cycles((Map<String, Object>) entity.getBody());
        return  cycle.getCycleNames();
    }



    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        return headers;
    }
}
