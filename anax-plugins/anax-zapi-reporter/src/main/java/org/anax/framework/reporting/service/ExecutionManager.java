package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExecutionManager {

    private final ZephyrService zapiService;
    private final TestCaseToIssueResolver issueResolver;

    @Autowired
    public ExecutionManager(ZephyrService zapiService, TestCaseToIssueResolver issueResolver) {
        this.zapiService = zapiService;
        if (this.zapiService instanceof ZephyrZAPIServerService) {
            log.info(">>> Instantiated ZAPI server service...");
        } else if (this.zapiService instanceof ZephyrZAPICloudService) {
            log.info(">>> Instantiated ZAPI cloud service...");
        }
        this.issueResolver = issueResolver;
    }

    /**
     * Update tc status
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttributes
     * @param tcStatus
     * @throws Exception
     */
    public void updateTestExecutions(String projectNameOrKey, String versionName, String cycleName, List<String> tcAttributes, String tcStatus) throws Exception {
        List<String> executionIds;

        if (tcAttributes.size() == 0) {
            log.error("There are no test cases contained in update list");
            throw new NoSuchFieldException();
        }

        executionIds = tcAttributes.stream().map(tc -> zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tc))).collect(Collectors.toList());
        executionIds.removeAll(Collections.singleton(""));//remove the execution ids that were not found and service returned ''

        Results results = Results.builder().executions(executionIds).status(tcStatus).build();
        if (zapiService instanceof ZephyrZAPICloudService) {
            results.setClearDefectMappingFlag(false);
            results.setTestStepStatusChangeFlag(false);
        }
        try {
            zapiService.updateBulkResults(results);
        } catch (Exception e) {
            log.error("Error during the update of TC: " + e.getMessage());
        }
    }

    /**
     * Update tc execution comment
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param comment
     */
    public void updateTestExecutionComment(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, String comment) {
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        if (!StringUtils.isEmpty(tcExecutionId)) {
            if (zapiService instanceof ZephyrZAPIServerService) {
                zapiService.updateTestExecutionComment(tcExecutionId, comment);
            } else {
                String tcExecutionIssueId = zapiService.getIssueExecutionIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
                zapiService.updateTestExecutionComment(projectNameOrKey, versionName, cycleName, tcExecutionId, tcExecutionIssueId, comment);
            }
        } else {
            log.error("Check: No test step found for this tc: {} at project: '{}' and version: '{}' in order to update test comment!!!", tcAttribute, projectNameOrKey, versionName);
        }
    }

    /**
     * Update test execution bugs
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param bugs
     */
    public void updateTestExecutionBugs(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, List<String> bugs) {
        if (!CollectionUtils.isEmpty(bugs)) {
            String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
            if (!StringUtils.isEmpty(tcExecutionId)) {
                if (zapiService instanceof ZephyrZAPIServerService) {
                    zapiService.updateTestExecutionBugs(tcExecutionId, bugs);
                } else {
                    String tcExecutionIssueId = zapiService.getIssueExecutionIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
                    zapiService.updateTestExecutionBugs(projectNameOrKey, versionName, cycleName, tcExecutionId, tcExecutionIssueId, bugs);
                }
                log.error("Bugs: '{}' was added on tc '{}' at project: '{}' and version: '{}' ", new HashSet<>(bugs).toString(), tcAttribute, projectNameOrKey, versionName);
            } else {
                log.error("Check: No test step found for this tc: {} at project: '{}' and version: '{}' in order to update test comment!!!", tcAttribute, projectNameOrKey, versionName);
            }
        }
    }

    /**
     * Update status for each test step, on not pass -> add screenshot and video if are enabled
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param status
     * @param testMethod
     */
    public void updateTestStepStatusAddAttachments(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, String status, TestMethod testMethod, File screenshot, File video) {
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        if (!tcExecutionId.isEmpty()) {
            if (zapiService instanceof ZephyrZAPIServerService) {
                updateTestStepStatusAddAttachmentsServer(tcExecutionId, tcAttribute, status, testMethod, screenshot, video);
            } else {
                updateTestStepStatusAddAttachmentsCloud(projectNameOrKey, versionName, cycleName, tcAttribute, status, testMethod, screenshot, video);
            }

        } else {
            log.error("Check: No test case execution Id found for tc: {} at project: '{}' and version: '{}' in order to update test step status/attachments!!!", tcAttribute, projectNameOrKey, versionName);
        }
    }

    private void updateTestStepStatusAddAttachmentsServer(String tcExecutionId, String tcAttribute, String status, TestMethod testMethod, File screenshot, File video) {
        String tcStepExecutionId = zapiService.getTestStepExecutionId(tcExecutionId, testMethod.getOrdering());
        if (!tcStepExecutionId.isEmpty()) {
            zapiService.updateTestStepStatus(tcStepExecutionId, status, testMethod);
            if (screenshot != null) {
                zapiService.addStepExecutionAttachments(tcStepExecutionId, screenshot);
            }
            if (video != null) {
                zapiService.addStepExecutionAttachments(tcStepExecutionId, video);
            }
        } else {
            log.info("No test step found for this tc: {} in order to update test steps status", tcAttribute);
        }
    }

    private void updateTestStepStatusAddAttachmentsCloud(String projectNameOrKey, String versionName, String cycleName, String testCaseName, String status, TestMethod testMethod, File screenshot, File video) {
        String stepId = getTestCaseStep(projectNameOrKey, versionName, cycleName, testCaseName, testMethod.getOrdering() + 1);
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(testCaseName));
        String issueId = zapiService.getIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(testCaseName));
        String stepResultId = zapiService.getTestStepResultId(tcExecutionId, issueId, testMethod.getOrdering() + 1);
        if (!tcExecutionId.isEmpty()) {
            zapiService.updateTestStepStatus(tcExecutionId, stepResultId, issueId, stepId, status);
            if (screenshot != null) {
                String projectId = zapiService.getProjectId(projectNameOrKey);
                String versionId = zapiService.getVersionId(projectNameOrKey, versionName);
                String cycleId = zapiService.getCycleId(projectNameOrKey, versionName, cycleName);
                zapiService.addStepExecutionAttachments(stepResultId, tcExecutionId, issueId, projectId, versionId, cycleId, screenshot);
            }
            if (video != null) {
                String projectId = zapiService.getProjectId(projectNameOrKey);
                String versionId = zapiService.getVersionId(projectNameOrKey, versionName);
                String cycleId = zapiService.getCycleId(projectNameOrKey, versionName, cycleName);
                zapiService.addStepExecutionAttachments(stepResultId, tcExecutionId, issueId, projectId, versionId, cycleId, video);
            }
        } else {
            log.info("No test step found for this tc: {} in order to update test steps status", testCaseName);
        }
    }


    /**
     * Returns a list of steps executions
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @return
     */
    public List<String> getTestCaseSteps(String projectNameOrKey, String versionName, String cycleName, String tcAttribute) {
        List<String> tcStepExecutionIds = new ArrayList<>();
        String tcExecutionIssueId;
        if (zapiService instanceof ZephyrZAPIServerService) {
            tcExecutionIssueId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        } else {
            tcExecutionIssueId = zapiService.getIssueExecutionIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        }
        if (!tcExecutionIssueId.isEmpty()) {
            tcStepExecutionIds = zapiService.getTestSteps(tcExecutionIssueId, projectNameOrKey);
        }
        return tcStepExecutionIds;

    }

    /**
     * Add attachment on each execution
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param tcAttribute
     * @param file
     */
    public void addExecutionAttachment(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, File file) {
        if (zapiService instanceof ZephyrZAPIServerService) {
            addExecutionAttachmentServer(projectNameOrKey, versionName, cycleName, tcAttribute, file);
        } else {
            addExecutionAttachmentCloud(projectNameOrKey, versionName, cycleName, tcAttribute, file);
        }
    }

    private void addExecutionAttachmentServer(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, File file) {
        String id = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        if (!id.isEmpty()) {
            zapiService.addTcExecutionAttachments(id, file);
        } else {
            log.error("Check: No test found for this tc: {} at project: '{}' and version: '{}' in order to add attachments!!!", tcAttribute, projectNameOrKey, versionName);
        }
    }

    private void addExecutionAttachmentCloud(String projectNameOrKey, String versionName, String cycleName, String tcAttribute, File file) {
        String tcExecutionId = zapiService.getIssueExecutionIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        String issueId = zapiService.getIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(tcAttribute));
        String projectId = zapiService.getProjectId(projectNameOrKey);
        String versionId = zapiService.getVersionId(projectNameOrKey, versionName);
        String cycleId = zapiService.getCycleId(projectNameOrKey, versionName, cycleName);
        if (!tcExecutionId.isEmpty()) {
            zapiService.addTcExecutionAttachments(tcExecutionId, "", issueId, projectId, versionId, cycleId, file);
        } else {
            log.error("Check: No test found for this tc: {} at project: '{}' and version: '{}' in order to add attachments!!!", tcAttribute, projectNameOrKey, versionName);
        }
    }

    /**
     * Returns a list of steps executions
     *
     * @param projectNameOrKey
     * @param versionName
     * @param cycleName
     * @param testCaseName
     * @return
     */
    public String getTestCaseStep(String projectNameOrKey, String versionName, String cycleName, String testCaseName, int ordering) {
        String issueId = zapiService.getIssueIdViaAttributeValue(projectNameOrKey, versionName, cycleName, resolveTcToIssue(testCaseName));
        String projectId = zapiService.getProjectId(projectNameOrKey);

        String stepId = null;
        if (!issueId.isEmpty()) {
            stepId = zapiService.getTestStepId(issueId, projectId, ordering);
        }
        return stepId;
    }


    //Resolve tc id either per label or per name or something else
    private String resolveTcToIssue(String testCaseAttribute) {
        return issueResolver.resolveTestCaseToIssue(testCaseAttribute);
    }
}
