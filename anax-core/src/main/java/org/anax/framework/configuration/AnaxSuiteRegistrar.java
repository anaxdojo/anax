package org.anax.framework.configuration;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;

import java.lang.reflect.Method;
import java.util.List;

public interface AnaxSuiteRegistrar {
    Suite registerSuite(String name);

    Test registerTest(Object bean, String beanName, int priority, List<Suite> rgSuites);

    void registerBeforeTest(Test test, Method method);

    void registerAfterTest(Test test, Method method);

    TestMethod registerPrecondition(Test test, Method method, boolean skip);

    TestMethod registerPostcondition(Test test, Method method, boolean skip);

    TestMethod registerTestMethod(Test test, Method method, int ordering, boolean skip);
}
