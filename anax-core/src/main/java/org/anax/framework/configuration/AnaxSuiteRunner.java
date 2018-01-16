package org.anax.framework.configuration;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.model.TestResult;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Component
@Slf4j
public class AnaxSuiteRunner {

    Map<String,Suite> suitesMap = new HashMap<>();

    boolean shouldAlsoExecute = false;

    @Autowired
    AnaxTestReporter reporter;

    public void createExecutionPlan(boolean executePlan) {
        shouldAlsoExecute = executePlan;

        if (shouldAlsoExecute) { //TODO handle the boolean for execution or display
            log.info("Executing test Suites: {}", suitesMap.keySet());
        } else {
            log.info("Planning for execution of Suites: {}", suitesMap.keySet());
        }
        suitesMap.keySet().stream().forEach( (String name) -> {
            try {
                final Suite suite = suitesMap.get(name);

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                reporter.setOutput(bout);
                reporter.startTestSuite(suite);
                executeTestSuite(suite);

                reporter.setSystemError(suite.getErr().toString());
                reporter.setSystemOutput(suite.getOut().toString());
                reporter.endTestSuite(suite);

                log.info("Report: {}", bout.toString());

            } catch (ReportException rpe) {
                log.error("Failed to initialize, check reports subsystem {}", rpe.getMessage(),rpe);
            }
        });
    }

    public void executeTestSuite(Suite suite) {
        log.info("--------------");
        log.info("SUITE START: {}", suite.getName());

        List<Test> copy = Lists.newArrayList(suite.getTests());
        copy.sort(Comparator.comparingInt(Test::getPriority));
        copy.forEach(test -> {
            reporter.startTest(test);
            executeTest(suite, test);
            reporter.endTest(test);
        });
        log.info("SUITE END: {}", suite.getName());

    }

    private void executeTest(Suite suite, Test test) {
        AtomicBoolean globalSkip = new AtomicBoolean(false);

        log.info("--------------");

        //sort by ordering
        List<TestMethod> testMethods = Lists.newArrayList(test.getTestMethods()).stream().filter(testMethod -> !testMethod.isSkip()).collect(Collectors.toList());
        log.info("Test: {} - steps: {}, skipped: {}", test.getTestBean().getClass().getName(), test.getTestMethods().size(), test.getTestMethods().size() - testMethods.size());

        testMethods.sort(Comparator.comparingInt(TestMethod::getOrdering));

        //before testmethod:
        test.getTestBeforeMethods().forEach(tm -> {
            log.info("---- BEFORE START: {}", tm.getTestMethod());
            TestResult result = executeRecordingResult(suite, test, tm, false);
            if (result.notPassed()) { // if before is skipped, execute no other method - all are skipped.
                globalSkip.set(true);
            }
            log.info("---- BEFORE END: {}", tm.getTestMethod());
        });

        testMethods.forEach(testMethod -> {
            AtomicBoolean localSkip = new AtomicBoolean(false);
            try {
                if (globalSkip.get()) {
                    reporter.addSkipped(test, testMethod, "Skipped due to @AnaxBefore failure");
                } else {
                    log.info("---- STEP START: {} ---", testMethod.getTestMethod());

                    //precondition:
                    testMethod.getPreconditions().forEach(tp -> {
                        log.info("---- PRECON START: {}", tp.getTestMethod());
                        if (localSkip.get() == false) {
                            TestResult result = executeRecordingResult(suite, test, tp, false);
                            if (result.notPassed()) {
                                localSkip.set(true);
                            }
                        }
                        log.info("---- PRECON END: {}", tp.getTestMethod());
                    });

                    //execute method!
                    if (localSkip.get() == false) {
                        TestResult result = executeRecordingResult(suite, test, testMethod, true);
                        if (result.notPassed()) {
                            localSkip.set(true);
                            if (result.isInError()) {
                                reporter.addError(test, testMethod, result.getThrowable());
                            } else if (result.isFailed()) {
                                reporter.addFailure(test,testMethod, result.getThrowable());
                            }
                        }
                    }
                    //postcondition:
                    testMethod.getPostconditions().forEach(tp -> {
                        log.info("---- POSTCON START: {}", tp.getTestMethod());
                        if (localSkip.get() == false) {
                            TestResult result = executeRecordingResult(suite, test, tp, false);
                            if (result.notPassed()) {
                                localSkip.set(true);
                            }
                        }
                        log.info("---- POSTCON END: {}", tp.getTestMethod());
                    });


                }
            } finally {
                log.info("---- STEP END: {} ---", testMethod.getTestMethod());

            }
        });

        //after testmethod:
        test.getTestAfterMethods().forEach(tm -> {
            log.info("AFTER START: {}", tm.getTestMethod());
            TestResult result = executeRecordingResult(suite, test, tm, false);
            if (result.notPassed()) { // if before is skipped, execute no other method - all are skipped.
                globalSkip.set(true);
            }
            log.info("AFTER END: {}", tm.getTestMethod());

        });
    }

    private TestResult executeRecordingResult(Suite suite, Test test, TestMethod tm, boolean isTest) {

        TestResult result = new TestResult();
        // capture stream for out
        ByteArrayOutputStream storedOut = new ByteArrayOutputStream();
        ByteArrayOutputStream storedErr = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        System.setOut( new PrintStream( new DuplicatingOutputStream(originalOut, storedOut) ));
        System.setErr( new PrintStream( new DuplicatingOutputStream(originalErr, storedErr)));
        log.trace("Replaced out and error streams with recording versions");
        //execute method
        log.debug("About to execute {} ", tm.getTestMethod());

        long execTime = 0;
        try {
            long t0 = System.currentTimeMillis();
            tm.getTestMethod().invoke(test.getTestBean());
            long t1 = System.currentTimeMillis();
            //if we're here, this was executed.
            execTime = t1-t0;
            log.debug("Test Method {} was executed", tm.getTestMethod());
        } catch (ReflectiveOperationException e) {
            result.setInError(true);
            result.setThrowable(e);
            log.info("Method {} threw exception {}", tm.getTestMethod(), e);
        } catch (RuntimeException e) { // runtime exceptions come from Assertions
            result.setFailed(true);
            result.setThrowable(e);
            log.info("Method {} threw exception {}", tm.getTestMethod(), e);
        } catch (Throwable e) {
            result.setInError(true);
            result.setThrowable(e);
            log.info("Method {} threw exception {}", tm.getTestMethod(), e);
        } finally {
            result.setStdError(storedErr.toString());
            result.setStdOutput(storedOut.toString());

            suite.getOut().append(result.getStdOutput());
            suite.getErr().append(result.getStdError());
            if (isTest) {
                if (result.isFailed()) {
                    suite.addFailed();
                } else if (result.isInError()) {
                    suite.addError();
                } else if (result.isSkipped()) {
                    suite.addSkip();
                } else {
                    suite.addRun();
                }
            }
            suite.addExecutionTime(execTime);
        }
        // replace original stream
        System.setOut(originalOut);
        System.setErr(originalErr);
        log.trace("Replaced out and error streams with original versions");


        return result;
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

    private class DuplicatingOutputStream extends OutputStream {
        private final OutputStream firstStream;
        private final OutputStream secondStream;

        public DuplicatingOutputStream(OutputStream firstStream, OutputStream secondStream) {
            this.firstStream = firstStream;
            this.secondStream = secondStream;
        }

        @Override
        public void write(int b) throws IOException {
            firstStream.write(b);
            secondStream.write(b);
        }

        @Override
        public void write(byte b[]) throws IOException {
            firstStream.write(b);
            secondStream.write(b);
        }
        public void write(byte b[], int off, int len) throws IOException {
            firstStream.write(b,off,len);
            secondStream.write(b,off,len);
        }
    }
}
