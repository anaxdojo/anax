package org.anax.framework.configuration;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;

public class ParallelPlanRunner {

    private final Suite suite;
    private final Test test;

    public ParallelPlanRunner(Suite suite, Test test) {
        this.suite = suite;
        this.test = test;
    }

    public void executeAndWait() {
        //TODO spawn new process, and pass suite and test as arguments.
    }
}
