package org.anax.framework.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Test {

    int priority;

    Object testBean;
    String testBeanName;
    String[] features;

    String testBeanDescription;

    @Builder.Default List<TestMethod> testMethods = new ArrayList<>();

    @Builder.Default List<TestMethod> testBeforeMethods = new ArrayList<>();
    @Builder.Default List<TestMethod> testAfterMethods = new ArrayList<>();


    @Builder.Default List<TestMethod> testPreconditions = new ArrayList<>();
    @Builder.Default List<TestMethod> testPostconditions = new ArrayList<>();
    @Builder.Default List<String> testIssues = new ArrayList<>();

}
