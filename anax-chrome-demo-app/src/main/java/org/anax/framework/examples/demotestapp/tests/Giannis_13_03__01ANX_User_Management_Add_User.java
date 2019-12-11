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

//Failed
@AnaxTest(value = "Results Analysis")
@Component
@Slf4j
public class Giannis_13_03__01ANX_User_Management_Add_User {

    @Autowired
    protected Verify            verify;

    @AnaxBeforeTest
    public void before() throws Exception{

    }

    @AnaxPreCondition(methodNames = "pre")
    @AnaxTestStep(description = "Verify user creation")
    public void test_step1() throws Exception{
        Assert.isTrue(1==2);
    }


    @AnaxPreCondition(methodNames = "pre1")
    @AnaxTestStep(description = "Verify user update", ordering = 1)
    public void test_step2() throws Exception{
        Assert.isTrue(1==1);
    }

    @AnaxPreCondition(methodNames = "pre")
    @AnaxTestStep(description = "Verify user deletion",ordering = 2)
    public void test_step3() throws Exception{
        Assert.isTrue(1==1);
    }


    public void pre(){
        log.info("Precondition");
    }

    public void pre1(){
        log.info("Failed Precondition");
        Assert.isTrue(1==2);
    }
}
