package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@AnaxTest(value = "Debug")
@Component
@Slf4j
public class Anax_Jira_TC135_MyTestNo2 {

    @Autowired
    protected Verify verify;

    @AnaxTestStep(description = "Given then ")
    public void test_step1() throws Exception{
        log.info("This is the test info passed to console");
        Assert.isTrue(1==2);
    }

    @AnaxTestStep(ordering = 1)
    public void test_step2() throws Exception{
        Assert.isTrue(1==1);
    }

    @AnaxTestStep(ordering = 2, skip = true)
    public void test_step3() throws Exception{
        Assert.isTrue(1==1);
    }
}
