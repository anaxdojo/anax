package org.anax.framework.reporting.service.impl;

import org.anax.framework.reporting.service.TestCaseToIssueResolver;

public class TestCaseToLabelResolver implements TestCaseToIssueResolver {


    @Override
    public String resolveTestCaseToIssue(String testCase) {
        testCase = testCase.replace("__","-");
        return testCase.substring(0,testCase.indexOf("ANX"));
    }
}
