package org.anax.framework.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Suite {
    private String name;
    @Builder.Default private List<Test> tests = new ArrayList<>();

    int executedTests;
    int skippedTests;
    int failedTests;
    int erroredTests;

    int totalRunTime;

    @Builder.Default StringBuilder out = new StringBuilder();
    @Builder.Default StringBuilder err = new StringBuilder();

    public void addFailed() {
        failedTests++;
    }

    public void addError() {
        erroredTests++;
    }

    public void addSkip() {
        skippedTests++;
    }

    public void addRun() {
        executedTests++;
    }

    public void addExecutionTime(long execTime) {
        totalRunTime += execTime;
    }
}
