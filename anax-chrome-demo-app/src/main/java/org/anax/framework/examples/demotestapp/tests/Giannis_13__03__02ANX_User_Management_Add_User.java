package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.*;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

//Pass
@AnaxTest(value = "Results Analysis")
@Component
@Slf4j
public class Giannis_13__03__02ANX_User_Management_Add_User {

    @Autowired
    protected Verify            verify;

    @AnaxBeforeTest
    public void before() throws Exception{
    }

    @AnaxIssues(issueNames = "https://jira.persado.com/browse/RON-2237")
//    @AnaxIssuesContainer({@AnaxIssues(issueNames = "https://jira.persado.com/browse/RON-2237"),@AnaxIssues(issueNames = "https://jira.persado.com/browse/RON-2237")})
    @AnaxTestStep(description = "Verify PP creation")
    public void test_step1() throws Exception{
        Assert.isTrue(1==2);
    }

    @AnaxTestStep(description = "Verify PP update", ordering = 1)
    public void test_step2() throws Exception{
        Assert.isTrue(1==3);
    }

    @AnaxTestStep(description = "Verify PP deletion" , ordering = 2)
    public void test_step3() throws Exception{
    }
}
