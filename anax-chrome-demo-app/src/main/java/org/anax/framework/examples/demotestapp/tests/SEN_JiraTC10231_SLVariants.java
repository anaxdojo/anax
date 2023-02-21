package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxBeforeTest;
import org.anax.framework.annotations.AnaxIssues;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@AnaxTest("SEN-10231")
@Component
@Slf4j
public class SEN_JiraTC10231_SLVariants {

    @AnaxBeforeTest
    public void test_Before() throws Exception{
        log.info("Class2: Runs once Before Class");
        Assert.isTrue(1==1);
    }

    @AnaxIssues(issueNames = "https://jira.persado.com/browse/RON-2237")
    @AnaxTestStep(description = "Verify PP creation")
    public void test_step1() throws Exception{
        Assert.isTrue(1==2);
    }

    @AnaxTestStep(description = "Verify PP creation" , ordering = 1)
    public void test_step2() throws Exception{
        Assert.isTrue(1==1);
    }

    @AnaxTestStep(description = "Verify PP creation" , ordering = 2)
    public void test_step3() throws Exception{
        Assert.isTrue(1==2);
    }
}
