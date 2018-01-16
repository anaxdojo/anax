package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.*;
import org.anax.framework.configuration.AnaxDriver;
import org.anax.framework.controllers.WebController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@AnaxTest(priority = 1)
@Component
@Slf4j
public class TestGoogle2 {

    @Autowired
    WebController controller;


    @AnaxBeforeTest
    public void test_Before() throws Exception{
        controller.navigate("www.google.gr");
        log.info("Class2: Runs once Before Class");
    }

    @AnaxTestStep
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
}
