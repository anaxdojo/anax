package org.anax.framework.model;

import lombok.Data;

@Data
public class TestResult {

    boolean skipped;
    boolean inError;
    boolean failed;

    String stdOutput;
    String stdError;

    Throwable throwable;

    public boolean notPassed() {
        return skipped || inError || failed;
    }

}
