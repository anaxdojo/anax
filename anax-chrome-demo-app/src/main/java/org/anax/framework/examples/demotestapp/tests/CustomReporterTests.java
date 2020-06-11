package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxBeforeTest;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.springframework.stereotype.Component;

@AnaxTest(value = "Custom Reporter Tests")
@Component
@Slf4j
public class CustomReporterTests {

    @AnaxBeforeTest
    public void test_Before() throws Exception{
        log.info("CustomReporterTests: Before Class");
    }

    @AnaxTestStep(ordering = 1)
    public void test_step1(){
        log.info("CustomReporterTests: Test step 1");
    }
}
