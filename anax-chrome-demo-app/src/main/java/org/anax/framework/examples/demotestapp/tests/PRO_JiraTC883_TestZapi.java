package org.anax.framework.examples.demotestapp.tests;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxTest;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.integrations.CycleCreator;
import org.anax.framework.integrations.ExecutionManager;
import org.anax.framework.integrations.pojo.CycleInfo;
import org.anax.framework.integrations.service.ZapiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@AnaxTest(value = "Debug")
@Component
@Slf4j
public class PRO_JiraTC883_TestZapi {

    @Autowired
    protected CycleCreator      cycleCreator;

    @Autowired
    protected ExecutionManager  executionManager;

    @Autowired
    protected ZapiServiceImpl   zapiService;

    @AnaxTestStep
    public void test_step1() throws Exception{
        CycleInfo cycleInfo  = CycleInfo.builder().build();//Geno 19.9.hot1
        cycleCreator.createCycleInVersion("Genopedia","Geno 19.9.hot1","Results Analysis", cycleInfo);
        zapiService.getCycleTCIdViaLabel("Genopedia","Geno 19.9.hot1","Results Analysis","giannis13",cycleInfo);
    }
}
