package org.anax.framework.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnaxBaseRunner implements CommandLineRunner{

    private final AnaxSuiteRunner suiteRunner;

    public AnaxBaseRunner(@Autowired AnaxSuiteRunner suiteRunner) {
        this.suiteRunner = suiteRunner;
    }

    public void run(String... strings) throws Exception {
        log.info("------------------------------------------------");
        log.info("Anax - a modern day testing automation framework");
        log.info("------------------------------------------------");

        log.info("");
        log.info("");
        log.info("");

        for (String option : strings) {
            log.debug("Option: {}", option);
        }

        final boolean planFailed = suiteRunner.createExecutionPlan(true);

        if (planFailed) {
            log.info("Plan failed, make jenkins job unstable");
            System.exit(100);
        } else {
            log.info("Plan passed");
            System.exit(0);
        }
    }
}
