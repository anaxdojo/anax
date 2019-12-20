package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
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
     * @param tcNames
     * @param tcStatus
     * @throws Exception
     */
    public void updateTestExecutions(String projectName, String versionName, String cycleName, List<String> tcNames, String tcStatus) throws Exception {
        List<String> executionIds;

        if(tcNames.size()==0){
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        executionIds = tcNames.stream().map(tc->zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tc))).collect(Collectors.toList());

        Results results = Results.builder().executions(executionIds).status(tcStatus).build();
        try{ zapiService.updateBulkResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
    }

    public void updateTestExecutionComment(String projectName, String versionName, String cycleName, String tcName, String comment){
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcName));
        zapiService.updateTestExecutionComment(tcExecutionId,comment);
    }

    /**
     * Update status for each test step, on not pass -> add screenshot and video if are enabled
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcName
     * @param status
     * @param testMethod
     */
    public void updateTestStepStatusAddAttachments(String projectName, String versionName, String cycleName, String tcName, String status, TestMethod testMethod, File screenshot, File video){
        String tcStepExecutionId;
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcName));
        if(!tcExecutionId.isEmpty()) {
            tcStepExecutionId = zapiService.getTestStepExecutionId(tcExecutionId, testMethod.getOrdering());
            if (!tcStepExecutionId.isEmpty()) {
                zapiService.updateTestStepStatus(tcStepExecutionId, status,testMethod);
                if(screenshot != null){zapiService.addStepExecutionAttachments(tcStepExecutionId,screenshot);}
                if(video != null){zapiService.addStepExecutionAttachments(tcStepExecutionId,video);}
            } else {
                log.info("No test step found for this tc: {} in order to update test steps status", tcName);
            }
        }else{
            log.error("ERROR: No test case execution Id found for tc: {}", tcName);
        }
    }


    /**
     * Returns a list of steps executions
     * @param projectName
     * @param versionName
     * @param cycleName
     * @param tcName
     * @return
     */
    public List<String> getTestCaseSteps(String projectName, String versionName, String cycleName, String tcName){
        List<String> tcStepExecutionIds = new ArrayList<>();
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectName,versionName,cycleName, resolveTcToIssue(tcName));
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
     * @param tcName
     * @param file
     */
    public void addExecutionAttachment(String projectName, String versionName, String cycleName, String tcName, File file){
        zapiService.addTcExecutionAttachments(projectName,versionName,cycleName, resolveTcToIssue(tcName),file);
    }


    //Resolve tc id either per label or per name or something else
    private String resolveTcToIssue(String testCaseName){
        return issueResolver.resolveTestCaseToIssue(testCaseName);
    }
}
