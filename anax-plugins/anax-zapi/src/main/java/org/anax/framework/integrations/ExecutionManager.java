package org.anax.framework.integrations;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.ExecutionStatus;
import org.anax.framework.integrations.pojo.Results;
import org.anax.framework.integrations.service.TestCaseToIssueResolver;
import org.anax.framework.integrations.service.ZapiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public void updateTestExecutions(String projectName, String versionName, String cycleName, List<String> tcNames, ExecutionStatus status) throws Exception {
        List<String> executionIds;

        if(tcNames.size()==0){
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        executionIds = tcNames.stream().map(tc->zapiService.getIssueIdViaLabel(projectName,versionName,cycleName,convertLabel(tc))).collect(Collectors.toList());

        Results results = Results.builder().executions(executionIds).status(status.getStatusId()).build();
        try{ zapiService.updateResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
    }



    private String convertLabel(String testCaseName){
        return issueResolver.resolveTestCaseToIssue(testCaseName);
    }
}
