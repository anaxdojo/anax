package org.anax.framework.annotations;


import lombok.extern.slf4j.Slf4j;
import org.anax.framework.configuration.AnaxSuiteRunner;
import org.anax.framework.model.*;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

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
//--------------------------------- Setting Up the TestStep  ---------------------------------------------------------------------
                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxTestStep.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxTestStep testStep = (AnaxTestStep) testAnnotation;
                    if(!testStep.dataprovider().isEmpty()){
                        Object providerBean = context.getBean(testStep.dataprovider());
                        if(providerBean instanceof DataProvider){
                            DataProvider dataProvider =(DataProvider) providerBean;
                            final List objects = dataProvider.provideTestData();
                            int bound = objects.size();
                            for (int nbr = 0; nbr < bound; nbr++) {
                                mainTestMethod.set(
                                        suiteRunner.registerTestMethod(test, method, testStep.description()
                                                , testStep.ordering(), testStep.skip(), objects.get(nbr),null));
                            }
                        }
                    }
                    else if(!testStep.datasupplier().isEmpty()){
                        Object supplierBean = context.getBean(testStep.datasupplier());
                        if(supplierBean instanceof DataSupplier){
                            DataSupplier dataSupplier =(DataSupplier) supplierBean;
                            Stream<Supplier> mySupplier =  dataSupplier.supplyResults();
                            mySupplier.forEach(s->
                                    mainTestMethod.set(
                                            suiteRunner.registerTestMethod(test, method, testStep.description()
                                                    , testStep.ordering(), testStep.skip(),null ,s)));
                        }
                    }
                    else {
                        mainTestMethod.set(suiteRunner.registerTestMethod(test, method,testStep.description() ,testStep.ordering()
                                , testStep.skip(), null,null));
                    }
                });
//---------------------------------------------------------------------------------------------------------------------------------
//--------------------------------- Setting Up the TestStep Preconditions ---------------------------------------------------------
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
//---------------------------------------------------------------------------------------------------------------------------------
// --------------------------------- Setting Up the TestStep Postconditions -------------------------------------------------------
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
//--------------------------------------------------------------------------------------------------------------------------------
// --------------------------------- Setting Up the AfterTest ---------------------------------------------------------------------
                Arrays.stream(declaredAnnotations).filter(item -> item.annotationType() == AnaxAfterTest.class)
                        .findFirst().ifPresent(testAnnotation -> {
                    AnaxAfterTest afterStep = (AnaxAfterTest) testAnnotation;
                    suiteRunner.registerAfterTest(test, method);
                });
//---------------------------------------------------------------------------------------------------------------------------------
            });



        });

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
