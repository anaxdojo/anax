package org.anax.framework.annotations;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

@Component
public class AnaxTestAnnotationProcessor implements BeanPostProcessor {

    private final ApplicationContext context;

    public AnaxTestAnnotationProcessor(@Autowired ApplicationContext context) {this.context = context;}

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
        //class level:BeforeTest
        Arrays.stream(aClass.getAnnotations()).filter(classAnn->classAnn.annotationType() == AnaxBeforeTest.class).findFirst()
                .ifPresent(item->{
                    // add bean in order to run before methods annotation
                });

        // method level:
        ReflectionUtils.doWithMethods(aClass, method -> {
            Arrays.stream(method.getDeclaredAnnotations()).filter(item -> item.annotationType() == AnaxBeforeTestStep.class)
                    .findFirst().ifPresent(testAnnotation -> {
                AnaxBeforeTestStep beforeStep = (AnaxBeforeTestStep) testAnnotation;

                if (!beforeStep.skip()) {
                    //TODO this method needs to be added to our Test methods to execute BEFORE the step
                    // in our Test Framework bean
                }
            });

            Arrays.stream(method.getDeclaredAnnotations()).filter(item -> item.annotationType() == AnaxTestStep.class)
                    .findFirst().ifPresent(testAnnotation -> {
                AnaxTestStep testStep = (AnaxTestStep) testAnnotation;

                int order = testStep.ordering();
                boolean skip = testStep.skip();
                if (!skip) {
                    // add bean, method, order
                }
            });

            Arrays.stream(method.getDeclaredAnnotations()).filter(item -> item.annotationType() == AnaxAfterTestStep.class)
                    .findFirst().ifPresent(testAnnotation -> {
                AnaxAfterTestStep afterStep = (AnaxAfterTestStep) testAnnotation;

                if (!afterStep.skip()) {
                    //TODO this method needs to be added to our Test methods to execute AFTER the step
                    // in our Test Framework bean
                }
            });
        });

        //class level:AfterTest
        Arrays.stream(aClass.getAnnotations()).filter(classAnn->classAnn.annotationType() == AnaxAfterTest.class).findFirst()
                .ifPresent(item->{
                    // add bean in order to run after methods annotation
                });

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
