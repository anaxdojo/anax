package org.anax.framework.configuration;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Component
@Slf4j
public class AnaxSuiteRunner {

    Map<String,Suite> suitesMap = new HashMap<>();

    boolean shouldAlsoExecute = false;

    public void createExecutionPlan(boolean executePlan) {

        if (executePlan) {
            log.info("Executing detected test suites:");
        }

        log.info("Configured Suites: {}", suitesMap.keySet());
        suitesMap.keySet().stream().forEach( name -> {
            suitesMap.values().forEach(this::executeTestSuite);
        });
    }

    public void executeTestSuite(Suite suite) {
        log.info("--------------");
        log.info("Suite: {}", suite.getName());

        List<Test> copy = Lists.newArrayList(suite.getTests());
        copy.sort(Comparator.comparingInt(Test::getPriority));
        copy.forEach(this::executeTest);
    }

    private void executeTest(Test test) {
        log.info("--------------");
        log.info("Test: {} - Steps: {}", test.getTestBean().getClass().getName(), test.getTestMethods().size());

        //sort by ordering
        List<TestMethod> testMethods = Lists.newArrayList(test.getTestMethods());
        testMethods.sort(Comparator.comparingInt(TestMethod::getOrdering));

        testMethods.forEach(testMethod -> {
            log.info("---- {} ---", testMethod.getTestMethod().getName());
            //before testmethod:
            test.getTestBeforeMethods().forEach( tm -> log.info("Before: {}", tm.getTestMethod().getName()));

            //precondition:
            test.getTestPreconditions().forEach( tp -> log.info("Precondition: {}", tp.getTestMethod().getName()));
            log.info("Test Step: {}", testMethod.getTestMethod().getName());

            //postcondition:
            test.getTestPostconditions().forEach( tp -> log.info("Postcondition: {}", tp.getTestMethod().getName()));

            //after testmethod:
            test.getTestAfterMethods().forEach( tm -> log.info("After: {}", tm.getTestMethod().getName()));
        });

    }


    public Suite registerSuite(String name) {

        if (suitesMap.containsKey(name)) {
            return suitesMap.get(name);
        } else {
            Suite suite = Suite.builder().name(name).build();
            suitesMap.put(name,suite);
            return suite;
        }


    }

    public Test registerTest(Object bean, String beanName, int priority, List<Suite> rgSuites) {

        Test test = Test.builder().testBean(bean).testBeanName(beanName).priority(priority).build();
        for (Suite s : rgSuites) {
            if (!suitesMap.containsKey(s.getName())) {
                suitesMap.put(s.getName(),s);
            }
            s.getTests().add(test);
        }
        return test;
    }

    public void registerBeforeTest(Test test, Method method) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).build();
        test.getTestBeforeMethods().add(testMethod);
    }
    public void registerAfterTest(Test test, Method method) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).build();
        test.getTestAfterMethods().add(testMethod);
    }

    public TestMethod registerPrecondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPreconditions().add(testMethod);
        return testMethod;
    }

    public TestMethod registerPostcondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPostconditions().add(testMethod);
        return testMethod;
    }


    public TestMethod registerTestMethod(Test test, Method method, int ordering, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).ordering(ordering).skip(skip).build();
        test.getTestMethods().add(testMethod);
        return testMethod;
    }
}
