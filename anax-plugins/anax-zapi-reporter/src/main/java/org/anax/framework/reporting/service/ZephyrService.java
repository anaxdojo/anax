package org.anax.framework.reporting.service;

import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.Results;
import org.json.JSONObject;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.File;
import java.util.List;

public interface ZephyrService {

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getCycleId(String projectNameOrKey, String versionName, String cycleName, boolean initialSearch);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getProjectId(String projectKey);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getCycleIdUnderUnSchedule(String projectNameOrKey, String cycleName);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getVersionId(String projectKey, String versionName);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getIssueExecutionIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    JSONObject getIssueExecutionViaAttributeValue(String projectKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getIssueExecutionIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getTestStepExecutionId(String tcExecutionId, int ordering);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getTestStepResultId(String executionId, String issueId, int stepOrder);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    List getTestSteps(String tcExecutionIssueId, String projectKey);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getTestStepId(String issueId, String projectId, int ordering);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestStepStatus(String testStepExecutionId, String status, TestMethod testMethod);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void addTcExecutionAttachments(String executionId, File file);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void addTcExecutionAttachments(String entityId, String executionId, String issueId, String projectId, String versionId, String cycleId, File file);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void addStepExecutionAttachments(String executionStepId, File file);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void addStepExecutionAttachments(String stepResultId, String tcExecutionId, String issueId, String projectId, String versionId, String cycleId, File file);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void cloneCycleToVersion(String projectNameOrKey, String versionName, CycleClone cycleClone, String originalCycleName);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateBulkResults(Results results);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionComment(String tcExecutionID, String comment);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionComment(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionBugs(String tcExecutionID, List<String> bugs);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionBugs(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestStepStatus(String tcExecutionId, String stepResultId, String executionIssueId, String testStepId, String status);
}
