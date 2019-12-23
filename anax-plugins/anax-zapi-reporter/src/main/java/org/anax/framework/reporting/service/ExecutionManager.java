package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExecutionManager {

    private final ZephyrZAPIService zapiService;
    private final TestCaseToIssueResolver issueResolver;

    @Autowired
    public ExecutionManager(ZephyrZAPIService zapiService, TestCaseToIssueResolver issueResolver) {
        this.zapiService = zapiService;
        this.issueResolver = issueResolver;
    }

    /**
     * Update tc status
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcAttributes
     * @param tcStatus
     * @throws Exception
     */
    public void updateTestExecutions(String projectName, String versionName, String cycleName, List<String> tcAttributes, String tcStatus) throws Exception {
        List<String> executionIds;

        if(tcAttributes.size()==0){
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        executionIds = tcAttributes.stream().map(tc->zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tc))).collect(Collectors.toList());
        executionIds.removeAll(Collections.singleton(""));//remove the execution ids that were not found and service returned ''

        Results results = Results.builder().executions(executionIds).status(tcStatus).build();
        try{ zapiService.updateBulkResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
    }

    /**
     * Update tc execution comment
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param comment
     */
    public void updateTestExecutionComment(String projectName, String versionName, String cycleName, String tcAttribute, String comment){
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcAttribute));
        if(!StringUtils.isEmpty(tcExecutionId)) {
            zapiService.updateTestExecutionComment(tcExecutionId, comment);
        }else{
            log.error("Check: No test step found for this tc: {} at project: '{}' and version: '{}' in order to update test comment!!!", tcAttribute,projectName,versionName);
        }
    }

    /**
     * Update status for each test step, on not pass -> add screenshot and video if are enabled
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param status
     * @param testMethod
     */
    public void updateTestStepStatusAddAttachments(String projectName, String versionName, String cycleName, String tcAttribute, String status, TestMethod testMethod, File screenshot, File video){
        String tcStepExecutionId;
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcAttribute));
        if(!tcExecutionId.isEmpty()) {
            tcStepExecutionId = zapiService.getTestStepExecutionId(tcExecutionId, testMethod.getOrdering());
            if (!tcStepExecutionId.isEmpty()) {
                zapiService.updateTestStepStatus(tcStepExecutionId, status,testMethod);
                if(screenshot != null){zapiService.addStepExecutionAttachments(tcStepExecutionId,screenshot);}
                if(video != null){zapiService.addStepExecutionAttachments(tcStepExecutionId,video);}
            } else {
                log.info("No test step found for this tc: {} in order to update test steps status", tcAttribute);
            }
        }else{
            log.error("Check: No test case execution Id found for tc: {} at project: '{}' and version: '{}' in order to update test step status/attachments!!!", tcAttribute,projectName,versionName);
        }
    }


    /**
     * Returns a list of steps executions
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @return
     */
    public List<String> getTestCaseSteps(String projectName, String versionName, String cycleName, String tcAttribute){
        List<String> tcStepExecutionIds = new ArrayList<>();
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcAttribute));
        if(!tcExecutionId.isEmpty()) {
            tcStepExecutionIds = zapiService.getTestSteps(tcExecutionId);
        }
        return tcStepExecutionIds;

    }

    /**
     * Add attachment on each execution
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param file
     */
    public void addExecutionAttachment(String projectName, String versionName, String cycleName, String tcAttribute, File file){
        String id = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName,resolveTcToIssue(tcAttribute));
        if(!id.isEmpty()) {
            zapiService.addTcExecutionAttachments(id, file);
        }else{
            log.error("Check: No test found for this tc: {} at project: '{}' and version: '{}' in order to add attachments!!!", tcAttribute,projectName,versionName);
        }
    }


    //Resolve tc id either per label or per name or something else
    private String resolveTcToIssue(String testCaseAttribute){ return issueResolver.resolveTestCaseToIssue(testCaseAttribute); }
}
