package org.anax.framework.reporting.service;

/**
 * A dynamic resolver for mapping of test cases to Jira or Zephyr
 */
public interface TestCaseToIssueResolver {

    /**
     * resolves a testcase name to a possible issue name or other
     * identifier in Jira or Zephyr
     * @param testCase
     * @return
     */
    public String resolveTestCaseToIssue(String testCase);

}
