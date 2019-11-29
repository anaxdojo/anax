package org.anax.framework.integrations.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ZapiService {

    @Autowired
    @Qualifier("zapiRestTemplate")
    protected RestTemplate restTemplate;

    @Value("${zapi.url:https:NOT_CONFIGURED}") private String zapiUrl;
    @Value("${jira.url:https:NOT_CONFIGURED}") private String jiraUrl;

    /**
     * Find the correct object in a jsonarray based on the value of an attribute
     * @param jsonArray
     * @param attribute
     * @param attributeValue
     * @return
     * @throws JSONException
     */
    public static JSONObject filterJsonArray(JSONArray jsonArray, String attribute, String attributeValue) throws JSONException{
        JSONArray jsonArray1 = new JSONArray(jsonArray.toString().toLowerCase());
        return new JSONObject(new JSONArray(JsonPath.read(jsonArray1.toString(),"$[?(@."+attribute.toLowerCase()+" ==\""+attributeValue.toLowerCase()+"\")]").toString()).get(0).toString());
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
    public String getProjectId(String projectName) {
        ProjectList projectList = restTemplate.getForObject(zapiUrl + "util/project-list", ProjectList.class);
        LabelValue labelValue = projectList.getOptions().stream().filter(data -> data.getLabel().equals(projectName)).findFirst().orElse(null);
        if(labelValue == null)
            log.error("No Project found with this name: {}",projectName);
        return (labelValue != null) ? labelValue.getValue() : "";
    }

    /**
     * Get cycle id from cycle name at unschedule
     * @param projectName
     * @param cycleName
     * @return
     */
    public String getCycleIdUnderUnSchedule(String projectName, String cycleName){
        String projectId = getProjectId(projectName);
        ResponseEntity<Map> entity = restTemplate.exchange(zapiUrl + "cycle?projectId=" + projectId+"&versionId=-1", HttpMethod.GET, HttpEntity.EMPTY, Map.class);
        Map.Entry<String, Map<Object, Object>> result = new Cycles(entity.getBody()).getContents().entrySet().stream().filter(x->x.getValue().get("name").equals(cycleName)).findFirst().orElse(null);
        return (result != null) ? result.getKey() : null;
    }


    /**
     * Get version id from version name
     * @param projectId
     * @param versionName
     * @return
     */
    public String getVersionId(String projectId, String versionName){
        ResponseEntity<List<Version>> versions = restTemplate.exchange(jiraUrl +"project/"+projectId+"/versions", HttpMethod.GET, new HttpEntity<>(getHeaders()),  new ParameterizedTypeReference<List<Version>>() {});
        Version version =  versions.getBody().stream().filter(data->data.getName().equals(versionName)).findFirst().orElse(null);
        if(version == null)
            log.error("No version found with this name: {}",versionName);
        return (version != null) ? version.getId() : "";
    }

    /**
     * Update execution results as bulk
     * @param results
     */
    public void updateResults(Results results){
        restTemplate.exchange(zapiUrl+"execution/updateBulkStatus/",HttpMethod.PUT ,new HttpEntity(results, getHeaders()), Results.class);
    }

    /**
     * Returns the test id from label
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param label
     * @throws Exception
     */
        public String getIssueIdViaLabel(String projectName, String versionName, String cycleName, String label) {
            String projectId = getProjectId(projectName);
            String versionId = getVersionId(projectId, versionName);
            String cycleId = getCycleId(projectName, versionName, cycleName);

            try {
                Thread.sleep(1000);
                return filterJsonArray((JSONArray) new JSONObject(restTemplate.exchange(zapiUrl + "execution?projectId=" + projectId + "&versionId=" + versionId + "&cycleId=" + cycleId, HttpMethod.GET, new HttpEntity<>(getHeaders()), String.class).getBody()).get("executions"), "label", label).get("id").toString();
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Check !! Issue with this label was not found");
                return "";

            }
    }


    /**
     * Add attachement on execution
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param label
     * @param file
     */
    public void addTcExecutionAttachments(String projectName, String versionName, String cycleName, String label, File file){
        String id = getIssueIdViaLabel(projectName,versionName,cycleName,label);
        LinkedMultiValueMap postBody = new LinkedMultiValueMap();
        postBody.add("file", new FileSystemResource(file));

        restTemplate.exchange(zapiUrl+"attachment?entityId="+id+"&entityType=EXECUTION", HttpMethod.POST, new HttpEntity<>(postBody, getMultiPartHeaders()), String.class);
    }



    /**
     * Clone a cycle (including executions) from default cycle to specific cycle
     * @param projectName
     * @param versionName
     * @param cycleClone
     * @param originalCycleName
     */
    public void cloneCycleToVersion(String projectName,String versionName,CycleClone cycleClone,String originalCycleName){
        String projectId = getProjectId(projectName);
        String versionId = getVersionId(projectId,versionName);
        String cycleId = getCycleIdUnderUnSchedule(projectName,originalCycleName);

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

    private HttpHeaders getMultiPartHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Atlassian-Token", "nocheck");
        headers.setContentType( MediaType.MULTIPART_FORM_DATA );
        return headers;
    }
}
