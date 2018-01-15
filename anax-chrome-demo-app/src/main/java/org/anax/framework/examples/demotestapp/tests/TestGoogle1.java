package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxPreCondition;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@AnaxTest
@Component
@Slf4j
public class TestGoogle1 {

    @AnaxTestStep
    @AnaxPreCondition(methodName = {"pre"})
    public void test_step1(){
        log.info("Class1: Test step1");
    }

    @AnaxTestStep(ordering = 3)
    public void test_step3(){
        log.info("Class1: Test step3");
    }

    @AnaxTestStep(ordering = 4,skip = true)
    public void test_step4(){
        log.info("Class1: Test step4::SKIPPED");
        Assert.isTrue(false);
    }

    @AnaxTestStep(ordering = 1)
    public void test_step2(){
        log.info("Class1: Test step1");
    }

    public void pre(){
        log.info("Class1: Runs before test_step1");
    }

}
