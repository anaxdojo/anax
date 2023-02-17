package org.anax.framework.reporting.service;

import org.anax.framework.reporting.cache.Caches;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.RetryException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public interface JiraService {

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${jira.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${jira.retry.minDelay:1000}", maxDelayExpression = "${jira.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.PROJECTS)
    String getProjectId(String projectKey);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${jira.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${jira.retry.minDelay:1000}", maxDelayExpression = "${jira.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.VERSIONS)
    String getVersionId(String projectKey, String versionName);

    @Retryable(value = RetryException.class, maxAttemptsExpression = "${jira.retry.maxAttempts:5}", backoff = @Backoff(random = true, delayExpression = "${jira.retry.minDelay:1000}", maxDelayExpression = "${jira.retry.maxDelay:6000}"))
    @Cacheable(value = Caches.ISSUES)
    String getJiraIssueId(String issueKey);

}
