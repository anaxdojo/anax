package org.anax.framework.reporting.service;

import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.Results;
import org.json.JSONObject;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Retryable;

import java.io.File;
import java.util.List;

public interface ZephyrService {

    @Retryable(value = RetryException.class)
    String getCycleId(String projectNameOrKey, String versionName, String cycleName, boolean initialSearch);

    @Retryable(value = RetryException.class)
    String getProjectId(String projectKey);

    @Retryable(value = RetryException.class)
    String getCycleIdUnderUnSchedule(String projectNameOrKey, String cycleName);

    @Retryable(value = RetryException.class)
    String getVersionId(String projectKey, String versionName);

    @Retryable(value = RetryException.class)
    String getIssueExecutionIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class)
    JSONObject getIssueExecutionViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class)
    String getIssueExecutionIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class)
    String getIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class)
    String getTestStepExecutionId(String tcExecutionId, int ordering);

    @Retryable(value = RetryException.class)
    String getTestStepResultId(String executionId, String issueId, int stepOrder);

    @Retryable(value = RetryException.class)
    List getTestSteps(String tcExecutionIssueId, String projectKey);

    @Retryable(value = RetryException.class)
    String getTestStepId(String issueId, String projectId, int ordering);

    @Retryable(value = RetryException.class)
    void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod);

    @Retryable(value = RetryException.class)
    void addTcExecutionAttachments(String executionId, File file);

    @Retryable(value = RetryException.class)
    void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file);

    @Retryable(value = RetryException.class)
    void addStepExecutionAttachments(String executionStepId, File file);

    @Retryable(value = RetryException.class)
    void addStepExecutionAttachments(String stepResultId, String tcExecutionId, String issueId, String projectId, String versionId, String cycleId, File file);

    @Retryable(value = RetryException.class)
    void cloneCycleToVersion(String projectNameOrKey, String versionName, CycleClone cycleClone, String originalCycleName);

    @Retryable(value = RetryException.class)
    void updateBulkResults(Results results);

    @Retryable(value = RetryException.class)
    void updateTestExecutionComment(String tcExecutionID, String comment);

    @Retryable(value = RetryException.class)
    void updateTestExecutionComment(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment);

    @Retryable(value = RetryException.class)
    void updateTestExecutionBugs(String tcExecutionID, List<String> bugs);

    @Retryable(value = RetryException.class)
    void updateTestExecutionBugs(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs);

    @Retryable(value = RetryException.class)
    void updateTestStepStatus(String tcExecutionId, String stepResultId, String executionIssueId, String testStepId, String status);
}
