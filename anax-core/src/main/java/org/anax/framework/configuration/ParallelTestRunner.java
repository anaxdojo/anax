package org.anax.framework.configuration;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.util.parallel.ForkClient;
import org.anax.framework.util.parallel.TimeoutLimits;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.IOException;
import java.util.Arrays;

@SpringBootApplication(scanBasePackages = "org.anax.framework")
public class ParallelTestRunner {
    private final Suite suite;
    private final Test test;
    private ForkClient client;

    public ParallelTestRunner(Suite suite, Test test) {
        this.suite = suite;
        this.test = test;
    }

    public void executeAndWait() throws IOException {
        //TODO spawn new process, and pass suite and test as arguments.
        // has to be done on TOP level, not on the suite level - new spring context is required

        ForkClient client = new ForkClient(this.getClass().getClassLoader(), this,
                Arrays.asList("java"), TimeoutLimits.DEFAULTS);
        client.call("workerMain", "-Dsuite="+suite.getName(), "-Dtest="+test.getTestBeanName());


    }

    public void workerMain(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ParallelTestRunner.class);
        builder.run(args);
    }


}
