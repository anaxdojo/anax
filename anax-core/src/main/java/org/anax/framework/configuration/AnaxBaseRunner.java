package org.anax.framework.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@Slf4j
public class AnaxBaseRunner implements CommandLineRunner{

    private final AnaxSuiteRunner suiteRunner;

    @Value("${git.build.time}")
    String buildTime;
    @Value("${git.build.version}")
    String buildVersion;
    @Value("${git.commit.id.abbrev}")
    String buildCommitId;


    public AnaxBaseRunner(@Autowired AnaxSuiteRunner suiteRunner) {
        this.suiteRunner = suiteRunner;
    }

    public void run(String... strings) throws Exception {
        int parallel = 0;
        log.info("Anax {} ({}) built at {}", buildVersion, buildCommitId, buildTime);
        for (String option : strings) {
            log.debug("Option: {}", option);
            if (option.startsWith("-Dparallel=")) {
                // get parallelism
                String parallels = option.split(Pattern.quote("="))[1];
                try {
                    parallel = Integer.parseInt(parallels);
                    log.info("Parallel mode detected, parallel spawn set to {}", parallel);
                } catch (Exception e) {
                }
            }
        }

        if (parallel > 0) {
            //do parallel
            suiteRunner.createParallelPlan(parallel);
        } else {
            final boolean planFailed = suiteRunner.createExecutionPlan(true);
            if (planFailed) {
                log.info("Plan failed, exit will make CI unstable/fail [100]");
                System.exit(100);
            } else {
                log.info("Plan passed [0]");
                System.exit(0);
            }
        }
    }
}
