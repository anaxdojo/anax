package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxBeforeTest;
import org.anax.framework.annotations.AnaxPreCondition;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@AnaxTest(value = "Results Analysis")
@Component
@Slf4j
public class Giannis_13_03__01ANX_User_Management_Add_User {


    @Autowired
    protected Verify            verify;

    @AnaxBeforeTest
    public void before() throws Exception{

    }

    @AnaxTestStep
    public void test_step1() throws Exception{
    }

    @AnaxPreCondition(methodNames = "pre")
    @AnaxTestStep(ordering = 1)
    public void test_step2() throws Exception{
    }

    @AnaxTestStep(ordering = 2)
    public void test_step3() throws Exception{
        log.info("step 3");
    }


    public void pre(){
        Assert.isTrue(1==2);
    }
}
