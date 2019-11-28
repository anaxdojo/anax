package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxBeforeTest;
import org.anax.framework.annotations.AnaxPreCondition;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.examples.demotestapp.pageObjects.GooglePageObject;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AnaxTest()
@Component
@Slf4j
public class Anax_JiraTC134_MytestCase {

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
    }

    @AnaxTestStep(ordering = 1)
    @AnaxPreCondition(methodNames = {"division"})
    public void test_step2() throws Exception{
        verify.text(GooglePageObject.GooglePageLocators.LABEL_CALCULATOR_RESULT.get(),"7");
    }

    @AnaxTestStep(ordering = 2)
    @AnaxPreCondition(methodNames = {"failCondition"})
    public void test_step3(){
        verify.equals(1==2);
        log.info("Class1: Test step3");
        verify.equals(1==2);
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
    }
}
