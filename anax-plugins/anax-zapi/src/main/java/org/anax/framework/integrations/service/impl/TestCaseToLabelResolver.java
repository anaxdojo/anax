package org.anax.framework.integrations.service.impl;

import org.anax.framework.integrations.service.TestCaseToIssueResolver;

public class TestCaseToLabelResolver implements TestCaseToIssueResolver {


    @Override
    public String resolveTestCaseToIssue(String testCase) {
        testCase = testCase.replace("__","-");
        return testCase.substring(0,testCase.indexOf("ANX"));
    }
}
