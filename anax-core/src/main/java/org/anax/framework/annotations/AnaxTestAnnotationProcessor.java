package org.anax.framework.annotations;


import lombok.extern.slf4j.Slf4j;
import org.anax.framework.configuration.AnaxSuiteRunner;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class AnaxTestAnnotationProcessor implements BeanPostProcessor {

    private final ApplicationContext context;
    private final AnaxSuiteRunner suiteRunner;

    public AnaxTestAnnotationProcessor(@Autowired ApplicationContext context, @Autowired AnaxSuiteRunner suiteRunner) {
        this.context = context;
        this.suiteRunner = suiteRunner;
    }

    /**
     * for every bean that we receive, we check to see if the @Anax... annotations is declared on
     * it's methods or class. For every method that matches, we add this to our Test execution context
     * for later processing
     * @param bean the bean object
     * @param beanName the bean name
     * @return the object
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();

        //class level:AnaxTest
        Arrays.stream(aClass.getAnnotations())
            .filter(classAnn->classAnn.annotationType() == AnaxTest.class)
            .findFirst().ifPresent(classAnnotation ->{

            AnaxTest anaxTest = (AnaxTest)classAnnotation;
            // it may have more than one suites as targets, lets register them all:
            List<Suite> rgSuites = new ArrayList<>();

            for (String name: anaxTest.value()) {
                Suite r = suiteRunner.registerSuite(name);
                rgSuites.add(r);
            }

            Test test = suiteRunner.registerTest(bean, anaxTest.description(), beanName ,anaxTest.priority(), rgSuites);

            // then, on method level:
            ReflectionUtils.doWithMethods(aClass, method -> {
                final Annotation[] declaredAnnotations = method.getDeclaredAnnotations();

                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxBeforeTest.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxBeforeTest beforeTest = (AnaxBeforeTest) testAnnotation;

                    suiteRunner.registerBeforeTest(test, method, beforeTest.ordering());
                });

                //we need the AtomicReference to simulate the "effectively final"
                final AtomicReference<TestMethod> mainTestMethod = new AtomicReference<>();
                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxTestStep.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxTestStep testStep = (AnaxTestStep) testAnnotation;

                    mainTestMethod.set(suiteRunner.registerTestMethod(test, method, testStep.ordering(), testStep.skip()));
                });


                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxPreCondition.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxPreCondition preCondition = (AnaxPreCondition) testAnnotation;

                    final List<String> names = Arrays.asList(preCondition.methodNames());

                    Arrays.stream(aClass.getDeclaredMethods())
                        .filter(m -> names.contains(m.getName()))
                        .forEach( m -> {
                            final TestMethod preCMethod = suiteRunner.registerPrecondition(test, m, preCondition.skip());
                            mainTestMethod.get().getPreconditions().add(preCMethod);
                        });

                });

                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxPostCondition.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxPostCondition postCondition = (AnaxPostCondition) testAnnotation;
                    final List<String> names = Arrays.asList(postCondition.methodNames());

                    Arrays.stream(aClass.getDeclaredMethods())
                        .filter(m -> names.contains(m.getName()))
                        .forEach( m -> {
                            TestMethod postCMethod = suiteRunner.registerPostcondition(test, m, postCondition.skip());
                            mainTestMethod.get().getPostconditions().add(postCMethod);
                        });

                });

                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxAfterTest.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxAfterTest afterStep = (AnaxAfterTest) testAnnotation;
                    suiteRunner.registerAfterTest(test, method);
                });
            });



        });

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
