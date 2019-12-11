package org.anax.framework.reporting.model;

public enum ExecutionStatus {

    PASS("1"),
    FAIL("2"),
    SKIPPED("3"),
    BLOCKED("4");


    private final String statusId;

    ExecutionStatus(String statId) { this.statusId = statId;}

    public String getStatusId() {
        return statusId;
    }
}
