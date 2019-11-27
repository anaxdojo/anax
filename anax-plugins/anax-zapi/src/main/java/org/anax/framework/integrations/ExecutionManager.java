package org.anax.framework.integrations;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.integrations.pojo.ExecutionStatus;
import org.anax.framework.integrations.pojo.Results;
import org.anax.framework.integrations.service.ZapiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExecutionManager {

    @Autowired
    protected ZapiServiceImpl zapiService;

    public void updateTestExecutions(String projectName, String versionName, String cycleName, List<String> issueIds, ExecutionStatus status) throws NoSuchFieldException {
        List<String> jiraIssueIds;
        List<String> executionIds;

        if(issueIds.size()==0){
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        jiraIssueIds = issueIds.stream().filter(data->!data.equals("UnknownTest")).collect(Collectors.toList());
        executionIds = jiraIssueIds.stream().map(data->zapiService.getExecutionId(projectName,versionName,cycleName,data)).collect(Collectors.toList());

        Results results = Results.builder().executions(executionIds).status(status.getStatusId()).build();
        try{ zapiService.updateResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
    }
}
