package org.anax.framework.configuration;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Component
@Slf4j
public class AnaxSuiteRunner {

    Map<String,Suite> suitesMap = new HashMap<>();

    public void exposeExecutionPlan() {
        suitesMap.keySet().stream().forEach( name -> {
            log.info("Execution plan:");
            log.info("Suite: {}",name);
            log.info("Suite Content: {}", suitesMap.get(name));
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

    public void registerPrecondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPreconditions().add(testMethod);
    }

    public void registerPostcondition(Test test, Method method, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).skip(skip).build();
        test.getTestPostconditions().add(testMethod);
    }


    public void registerTestMethod(Test test, Method method, int ordering, boolean skip) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).ordering(ordering).skip(skip).build();
        test.getTestMethods().add(testMethod);
    }
}
