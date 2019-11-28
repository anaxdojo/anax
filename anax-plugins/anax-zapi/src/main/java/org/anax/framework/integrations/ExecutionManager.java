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

    public void updateTestExecutions(String projectName, String versionName, String cycleName, List<String> issueNames, ExecutionStatus status) throws Exception {
        List<String> executionIds;

        if(issueNames.size()==0){
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        executionIds = issueNames.stream().map(data->zapiService.getIssueIdViaLabel(projectName,versionName,cycleName,convertLabel(data))).collect(Collectors.toList());

        Results results = Results.builder().executions(executionIds).status(status.getStatusId()).build();
        try{ zapiService.updateResults(results); }catch(Exception e){ log.error("Error during the update of TC: "+e.getMessage()); }
    }



    private String convertLabel(String issueName){
        issueName = issueName.replace("__","-");
        return issueName.substring(0,issueName.indexOf("ANX"));
    }
}
