package org.anax.framework.configuration;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnaxDefaultSuiteRegistrar implements AnaxSuiteRegistrar {
    Map<String,Suite> suitesMap = new HashMap<>();

    @Override
    public Suite registerSuite(String name) {

        if (suitesMap.containsKey(name)) {
            return suitesMap.get(name);
        } else {
            Suite suite = Suite.builder().name(name).build();
            suitesMap.put(name,suite);
            return suite;
        }


    }

    @Override
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

    @Override
    public void registerBeforeTest(Test test, Method method) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).build();
        test.getTestBeforeMethods().add(testMethod);
    }

    @Override
    public void registerAfterTest(Test test, Method method) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).build();
        test.getTestAfterMethods().add(testMethod);
    }

    @Override
    public TestMethod registerPrecondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPreconditions().add(testMethod);
        return testMethod;
    }

    @Override
    public TestMethod registerPostcondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPostconditions().add(testMethod);
        return testMethod;
    }

    @Override
    public TestMethod registerTestMethod(Test test, Method method, int ordering, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).ordering(ordering).skip(skip).build();
        test.getTestMethods().add(testMethod);
        return testMethod;
    }
}
