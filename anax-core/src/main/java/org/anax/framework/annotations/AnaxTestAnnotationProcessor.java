package org.anax.framework.annotations;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;

@Component
public class AnaxTestAnnotationProcessor implements BeanPostProcessor {

    private final ApplicationContext context;

    public AnaxTestAnnotationProcessor(@Autowired ApplicationContext context) {
        this.context = context;
    }

    /**
     * for every bean that we receive, we check to see if the @AnaxTest annotation is declared on
     * it's methods. For every method that matches, we add this to our Test execution context
     * for later processing
     * @param bean the bean object
     * @param beanName the bean name
     * @return the object
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        ReflectionUtils.doWithMethods(bean.getClass(), method -> {
            for (Annotation ant : method.getDeclaredAnnotations()) {

                if (ant.annotationType() == AnaxTest.class) {
                    //TODO this method needs to be added to our Test methods to execute
                    // in our Test Framework bean
                    AnaxTest testAnnotation = (AnaxTest)ant;
                    int order = testAnnotation.ordering();
                    boolean skip = testAnnotation.skip();
                    if (!skip) {
                        // add bean, method, order
                    }
                } else if (ant.annotationType() == AnaxBeforeClass.class) {

                    //TODO this method needs to be added to our Test methods to execute BEFORE the class
                } else if (ant.annotationType() == AnaxAfterClass.class) {

                    //TODO this method needs to be added to our Test methods to execute AFTER the class
                }
            }
        });

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
