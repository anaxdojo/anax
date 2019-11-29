package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxBeforeTest;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.integrations.CycleCreator;
import org.anax.framework.integrations.ExecutionManager;
import org.anax.framework.integrations.service.ZapiService;
import org.anax.framework.testing.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;


@AnaxTest(value = "Results Analysis")
@Component
@Slf4j
public class Giannis_13_03__01ANX_User_Management_Add_User {

    @Autowired
    protected CycleCreator      cycleCreator;

    @Autowired
    protected ExecutionManager  executionManager;

    @Autowired
    protected ZapiService zapiService;

    @Autowired
    protected Verify            verify;

    @AnaxBeforeTest
    public void before() throws Exception{
        Assert.isTrue(1==2);
    }

    @AnaxTestStep
    public void test_step1() throws Exception{
    }

    @AnaxTestStep(ordering = 1)
    public void test_step2() throws Exception{
    }
}
