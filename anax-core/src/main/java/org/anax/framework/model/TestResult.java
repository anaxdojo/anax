package org.anax.framework.model;

import lombok.Data;

@Data
public class TestResult {

    boolean skipped;
    boolean inError;
    boolean failed;

    String stdOutput = "";
    String stdError = "";

    Throwable throwable;

    public boolean notPassed() {
        return skipped || inError || failed;
    }

    public void appendResult(TestResult result) {

        stdError = stdError.concat(result.stdError);
        stdOutput = stdOutput.concat(result.stdOutput);

        skipped = result.skipped? true:skipped;

        inError = result.inError? true:inError;

        failed = result.failed? true:failed;

        throwable = result.throwable == null ? throwable : result.throwable;
    }

}
