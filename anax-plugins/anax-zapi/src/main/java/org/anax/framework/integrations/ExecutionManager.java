package org.anax.framework.integrations;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.Results;
import org.anax.framework.integrations.service.TestCaseToIssueResolver;
import org.anax.framework.integrations.service.ZapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExecutionManager {

    private final ZapiService zapiService;
    private final TestCaseToIssueResolver issueResolver;

    @Autowired
    public ExecutionManager(ZapiService zapiService, TestCaseToIssueResolver issueResolver) {
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

        executionIds = tcNames.stream().map(tc->zapiService.getIssueIdViaLabel(projectName,versionName,cycleName,convertLabel(tc))).collect(Collectors.toList());

        Results results = Results.builder().executions(executionIds).status(tcStatus).build();
        try{ zapiService.updateResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
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
        zapiService.addTcExecutionAttachments(projectName,versionName,cycleName,convertLabel(tcName),file);
    }


    private String convertLabel(String testCaseName){
        return issueResolver.resolveTestCaseToIssue(testCaseName);
    }
}
