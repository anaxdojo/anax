package org.anax.framework.integrations.reporting;

import lombok.extern.java.Log;
import org.anax.framework.integrations.CycleCreator;
import org.anax.framework.integrations.ExecutionManager;
import org.anax.framework.integrations.pojo.ExecutionStatus;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.AnaxTestReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
@Log
public class ZapiReporting implements AnaxTestReporter {

    private String cycleName;
    private String version = "Geno 19.9.hot1";

    @Value("${jira.project:NOT_CONFIGURED}") private String project;
    @Value("${zapi.enabled:true}") private Boolean enabled;

    @Autowired
    protected CycleCreator cycleCreator;

    @Autowired
    protected ExecutionManager updateTests;

    private Set<String> passedTCs = new HashSet<String>();
    private Set<String> failedTCs = new HashSet<String>();
    private Set<String> skippedTCs = new HashSet<String>();
    private Set<String> errorTCs = new HashSet<String>();



    @Override
    public void startOutput(String reportDirectory, String suiteName){

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String out) {

    }

    @Override
    public void startTestSuite(Suite suite) {
        if(enabled) {
            cycleName = suite.getName();
            log.info("************* ZAPI Reporter Start() ***** " + this);
            log.info("********************************************\r\n\r\n");
            log.info("Create Cycle: " + suite.getName() + ", at project: " + project);
            log.info("********************************************\r\n\r\n");

            initialiseCycles(project, version, cycleName);
        }

    }

    @Override
    public boolean endTestSuite(Suite suite){
        if(enabled) {
            log.info("************* ZAPI Reporter onFinish() ***** " + this);
            log.info("********************************************\r\n\r\n");
            log.info("onFinish: Cycle: " + cycleName + ", version: " + version + ", manager: " + updateTests + " - " + this.toString());
            log.info("********************************************\r\n\r\n");

            log.info(passedTCs.toString());
            log.info(failedTCs.toString());
            log.info(skippedTCs.toString());
            log.info(errorTCs.toString());

            errorTCs.forEach(it -> passedTCs.remove(it));

            /**
             * Update Test Cases execution Status
             */
            try {
                if (passedTCs.size() != 0) {
                    log.info("Update as PASS the following TCs: " + passedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(passedTCs), ExecutionStatus.PASS);
                }
            } catch (Exception e) {
                log.info("The update of PASSED TCs on jira did not happen due to: " + e.getMessage());
            }

            try {
                if (failedTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + failedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(failedTCs), ExecutionStatus.FAIL);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }

            try {
                if (skippedTCs.size() != 0) {
                    log.info("Update as BLOCK the following TCs: " + skippedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(skippedTCs), ExecutionStatus.SKIPPED);
                }
            } catch (Exception e2) {
                log.info("The update on SKIPPED jira did not happen due to: " + e2.getMessage());
            }

            try {
                if (errorTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + errorTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(errorTCs), ExecutionStatus.FAIL);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }

        }
        return false;
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {

    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        if(enabled) {
            log.info("Identify if test has passed");
            if (!failedTCs.contains(test.getTestBeanName()) && !skippedTCs.contains(test.getTestBeanName())) {
                log.info("Added TC on the passedTCs is: " + test.getTestBeanName());
                passedTCs.add(test.getTestBeanName());
            }
        }
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        log.info("Added TC on the failedTCs is: "+test.getTestBeanName());
        failedTCs.add(test.getTestBeanName());
    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {
        log.info("Added TC on the skippedTCs is: "+test.getTestBeanName());
        skippedTCs.add(test.getTestBeanName());
    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t) {
        log.info("Added TC on the errorTCs is: "+test.getTestBeanName());
        errorTCs.add(test.getTestBeanName());
    }

    public final void initialiseCycles(String projectName, String versionName, String cycleName) {
        try{
            cycleCreator.createCycleInVersion(projectName,versionName.trim(), cycleName.trim());
        }catch (Exception e){
            log.info("Cycle on Jira was not created due to: "+e.getMessage());
        }
    }

}
