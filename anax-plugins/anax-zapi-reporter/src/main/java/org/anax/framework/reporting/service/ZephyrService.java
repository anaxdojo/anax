package org.anax.framework.reporting.service;

import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.Results;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public interface ZephyrService {

    String getCycleId(String projectNameOrKey, String versionName, String cycleName);

    String getProjectId(String projectKey);

    String getCycleIdUnderUnSchedule(String projectNameOrKey, String cycleName);

    String getVersionId(String projectKey, String versionName);

    String getIssueExecutionIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    JSONObject getIssueExecutionViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue);

    String getIssueExecutionIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    String getIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    String getTestStepExecutionId(String tcExecutionId, int ordering);

    String getTestStepResultId(String executionId, String issueId, int stepOrder);

    List getTestSteps(String tcExecutionIssueId, String projectKey);

    String getTestStepId(String issueId, String projectId, int ordering);

    void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod);

    void addTcExecutionAttachments(String executionId, File file);

    void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file);

    void addStepExecutionAttachments(String executionStepId, File file);

    void addStepExecutionAttachments(String stepResultId, String tcExecutionId, String issueId, String projectId, String versionId, String cycleId, File file);

    void cloneCycleToVersion(String projectNameOrKey, String versionName, CycleClone cycleClone, String originalCycleName);

    void updateBulkResults(Results results);

    void updateTestExecutionComment(String tcExecutionID, String comment);

    void updateTestExecutionComment(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment);

    void updateTestExecutionBugs(String tcExecutionID, List<String> bugs);

    void updateTestExecutionBugs(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs);

    void updateTestStepStatus(String tcExecutionId, String stepResultId, String executionIssueId, String testStepId, String status);
}
