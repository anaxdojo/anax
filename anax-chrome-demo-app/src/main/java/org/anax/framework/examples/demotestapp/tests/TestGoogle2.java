package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.*;
import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.controllers.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AnaxTest(value = "Debug")
@Component
@Slf4j
public class TestGoogle2 {

    @Autowired
    WebController controller;


    @AnaxBeforeTest
    public void test_Before() throws Exception{
        controller.navigate("http://www.google.gr");
        log.info("Class2: Runs once Before Class");
    }

    @AnaxTestStep
    @AnaxPreCondition(methodNames = {"preConditionOfTestStep1","preConditionOfTestStep2","preConditionOfTestStep3","preConditionOfTestStep4","preConditionOfTestStep10"})
    public void test_step1(){
        log.info("Class2: Test step1");
    }

    @AnaxPostCondition(methodNames = {"afterConditionOfTestStep1"})
    @AnaxTestStep(ordering = 1)
    public void test_step2(){
        log.info("Class2: Test step2");
    }

    @AnaxAfterTest
    public void test_step3(){
        log.info("Class2: After Test");
    }

    public void afterConditionOfTestStep1() {
        log.info("Class2:Runs once after Test step2");
    }

    public void preConditionOfTestStep1() {
        log.info("PreCondition 1");
    }

    public void preConditionOfTestStep2() {
        log.info("PreCondition 2");
    }

    public void preConditionOfTestStep3() {
        log.info("PreCondition 3");
    }

    public void preConditionOfTestStep4() {
        log.info("PreCondition 4");
    }

    public void preConditionOfTestStep10() {
        log.info("PreCondition 10");
    }
}
