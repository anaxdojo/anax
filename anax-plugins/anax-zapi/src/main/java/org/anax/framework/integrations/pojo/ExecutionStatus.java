package org.anax.framework.integrations.pojo;

/**
 * Created by gkogketsof on 3/21/14.
 */
public enum ExecutionStatus {

    PASS("1"),
    FAIL("2"),
    BLOCKED("4");


    private final String statusId;

    ExecutionStatus(String statId) { this.statusId = statId;}

    public String getStatusId() {
        return statusId;
    }
}
