package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.*;
import org.anax.framework.examples.demotestapp.pageObjects.GooglePageObject;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@AnaxTest(description = "Hello World Test")
@Component
@Slf4j
public class TestGoogle1 {

    @Autowired
    protected  GooglePageObject        googlePageObject;

    @Autowired
    protected Verify                  verify;

    @AnaxBeforeTest
    public void load(){
        log.info("First");
    }

    @AnaxBeforeTest(ordering = 1)
    public void create(){
        log.info("Second");
    }

    @AnaxTestStep(description = "Given then ")
    @AnaxPreCondition(methodNames = {"inputValuesToGoogle"})
    public void test_step1() throws Exception{
        log.info("This is the test info passed to console");
        verify.text(GooglePageObject.GooglePageLocators.LABEL_CALCULATOR_RESULT.get(),"7");
        Thread.sleep(2000);
    }

    @AnaxTestStep(ordering = 1)
    @AnaxPreCondition(methodNames = {"division"})
    public void test_step2() throws Exception{
        Thread.sleep(1000);
        verify.text(GooglePageObject.GooglePageLocators.LABEL_CALCULATOR_RESULT.get(),"7");
        Thread.sleep(5000);
    }

    @AnaxTestStep(ordering = 3)
    @AnaxPreCondition(methodNames = {"failCondition"})
    public void test_step3(){
        log.info("Class1: Test step3");
    }


    @AnaxTestStep(ordering = 4,skip = true)
    public void test_step4(){
        log.info("Class1: Test step4::SKIPPED");
        Assert.isTrue(false,"failed");
    }

    @AnaxTestStep(ordering = 5)
    @AnaxPostCondition(methodNames = {"failCondition"})
    public void test_step5(){
        log.info("Class1: Test step5");
    }

    @AnaxTestStep(ordering = 6)
    public void test_step6(){
        log.info("Class1: Test step6");
    }

    public void inputValuesToGoogle(){
        log.info("Class1: Runs once before test_step1");
        googlePageObject.inputSearchText("3+4");
    }

    public void division(){
        googlePageObject.pressCalculatorDivisionButton();
        googlePageObject.pressCalculatorNumber("7");
        googlePageObject.pressCalculatorEqualButton();
    }

    public void failCondition(){
        verify.equals(1==2);
    }
}
