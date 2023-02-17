package org.anax.framework.reporting.service;

import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.cache.Caches;
import org.anax.framework.reporting.model.CycleClone;
import org.anax.framework.reporting.model.Results;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.io.File;
import java.util.List;

public interface ZephyrService {

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.CYCLES, key = "#p0 + #p1 + #p2", unless = "#result == null")
    String getCycleId(String projectNameOrKey, String versionName, String cycleName, boolean initialSearch);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    String getCycleIdUnderUnSchedule(String projectNameOrKey, String cycleName);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.EXECUTION_IDS, unless = "#result == null")
    String getIssueExecutionIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue, String cycleId);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.EXECUTION_ISSUE_IDS, unless = "#result == null")
    String getIssueExecutionIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue, String cycleId);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.ISSUE_IDS, unless = "#result == null")
    String getIssueIdViaAttributeValue(String projectNameOrKey, String versionName, String cycleName, String attributeValue, String cycleId);

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
    void updateTestExecutionComment(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, String comment, String cycleId);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionBugs(String tcExecutionID, List<String> bugs);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestExecutionBugs(String projectNameOrKey, String versionName, String cycleName, String tcExecutionID, String tcExecutionIssueID, List<String> bugs, String cycleId);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${zapi.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${zapi.retry.minDelay:1000}", maxDelayExpression = "${zapi.retry.maxDelay:6000}"))
    void updateTestStepStatus(String tcExecutionId, String stepResultId, String executionIssueId, String testStepId, String status);
}
