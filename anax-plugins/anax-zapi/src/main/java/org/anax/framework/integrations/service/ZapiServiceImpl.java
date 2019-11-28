package org.anax.framework.integrations.service;

import com.jayway.jsonpath.JsonPath;
import org.anax.framework.integrations.pojo.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ZapiServiceImpl {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Value("${zapi.url:https://jira.persado.com/rest/zapi/1.0/}") private String zapiUrl;
    @Value("${jira.url:https://jira.persado.com/rest/api/2/}") private String jiraUrl;
    @Value("${jira.originalCycle:Unresolved_Unplanned}") private String originalCycle;

    /**
     * Find the correct object in a jsonarray based on the value of an attribute
     * @param jsonArray
     * @param attribute
     * @param attributeValue
     * @return
     * @throws JSONException
     */
    public static JSONObject filterJsonArray(JSONArray jsonArray, String attribute, String attributeValue) throws JSONException{
        return new JSONObject(new JSONArray(JsonPath.read(jsonArray.toString(),"$[?(@."+attribute+" ==\""+attributeValue+"\")]").toString()).get(0).toString());
    }

    /**
     * Get cycle id from cycle name at unschedule
     * @param projectName
     * @param cycleName
     * @return
     */
    public String getCycleId(String projectName, String versionName , String cycleName){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId+"&versionId="+versionId, HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Map.Entry<String, Map<Object, Object>> result = new Cycles(entity.getBody()).getContents().entrySet().stream().filter(x->x.getValue().get("name").equals(cycleName)).findFirst().orElse(null);
        return (result != null) ? result.getKey() : null;
    }


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
     * Get cycle id from cycle name at unschedule
     * @param projectName
     * @param cycleName
     * @return
     */
    public String getCycleIdUnderUnschedule(String projectName, String cycleName){
        String projectId = getProjectId(projectName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId+"&versionId=-1", HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Map.Entry<String, Map<Object, Object>> result = new Cycles(entity.getBody()).getContents().entrySet().stream().filter(x->x.getValue().get("name").equals(cycleName)).findFirst().orElse(null);
        return (result != null) ? result.getKey() : null;    }


    /**
     * Get issue id from issue name
     * @param issueName
     * @return
     */
    public String getIssueId(String issueName){
        return restTemplate.getForObject(jiraUrl+"issue/"+issueName, Issue.class).getId();
    }

    /**
     * Get version id from version name
     * @param projectId
     * @param versionName
     * @return
     */
    public String getVersionId(String projectId, String versionName){
        ResponseEntity<List<Version>> versions = restTemplate.exchange(jiraUrl +"project/"+projectId+"/versions", HttpMethod.GET, new HttpEntity<>(getHeaders()),  new ParameterizedTypeReference<List<Version>>() {});
        return versions.getBody().stream().filter(data->data.getName().equals(versionName)).findFirst().get().getId();
    }

    /**
     * Update execution results as bulk
     * @param results
     */
    public void updateResults(Results results){
        restTemplate.exchange(zapiUrl+"execution/updateBulkStatus/",HttpMethod.PUT ,new HttpEntity(results, getHeaders()), Results.class);
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
     * Update cycle info
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param cycleInfo
     */
    public void updateCycleInfo(String projectName,String versionName,String cycleName,CycleInfo cycleInfo){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleId(projectName,versionName,cycleName);

        cycleInfo.setProjectId(projectId);
        cycleInfo.setVersionId(versionId);
        cycleInfo.setId(cycleId);
        restTemplate.exchange(zapiUrl+"cycle",HttpMethod.PUT ,new HttpEntity(cycleInfo, getHeaders()), CycleInfo.class);
    }

    /**
     * Returns the test id from label
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param label
     * @param cycleInfo
     * @throws Exception
     */
    public void getCycleTCIdViaLabel(String projectName,String versionName,String cycleName,String label, CycleInfo cycleInfo) throws Exception{
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleId(projectName,versionName,cycleName);

        cycleInfo.setProjectId(projectId);
        cycleInfo.setVersionId(versionId);
        cycleInfo.setId(cycleId);
        filterJsonArray((JSONArray) new JSONObject(restTemplate.exchange(zapiUrl+"execution?projectId=" + projectId + "&versionId=" + versionId + "&cycleId=" + cycleId,HttpMethod.GET, new HttpEntity<>(getHeaders()),String.class).getBody()).get("executions"),"label",label).get("id");
    }

    /**
     * Clone a cyle (including executions) from default cycle to specific cycle
     * @param projectName
     * @param versionName
     * @param cycleClone
     * @param originalCycleName
     */
    public void cloneCycleToVersion(String projectName,String versionName,CycleClone cycleClone,String originalCycleName){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleIdUnderUnschedule(projectName,originalCycleName);

        cycleClone.setProjectId(projectId);
        cycleClone.setVersionId(versionId);
        cycleClone.setClonedCycleId(cycleId);
        restTemplate.exchange(zapiUrl+"cycle",HttpMethod.POST ,new HttpEntity(cycleClone, getHeaders()), CycleClone.class);
    }


    private HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        return headers;
    }
}
