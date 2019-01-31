package org.anax.framework.configuration;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.model.TestResult;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReportException;
import org.anax.framework.reporting.ReporterSupportsScreenshot;
import org.anax.framework.reporting.ReporterSupportsVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnaxSuiteRunner {

    @Autowired
    ApplicationContext context;

    private final AnaxTestReporter reporter;

    Map<String, Suite> suitesMap = new HashMap<>();

    boolean shouldAlsoExecute = false;

    @Value("${anax.report.directory:reports/}")
    String reportDirectory;
    @Value("${anax.exec.suite:ALL}")
    String executeSuite;
    @Value("${enable.video:true}")
    Boolean videoOn;
    @Value("${enable.screenshot:true}")
    Boolean screenshotOn;

    @Autowired
    WebController controller;

    public AnaxSuiteRunner(@Autowired AnaxTestReporter reporter) {
        this.reporter = reporter;
    }


    @PostConstruct
    public void postConstruct() {

        File dir = new File(reportDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }


    public boolean createExecutionPlan(boolean executePlan) {
        shouldAlsoExecute = executePlan;

        if (shouldAlsoExecute) { //TODO handle the boolean for execution or display
            log.info("Executing test Suites: {}", suitesMap.keySet());
        } else {
            log.info("Planning for execution of Suites: {}", suitesMap.keySet());
        }

        //configuring reporters here
        if (videoOn && reporter instanceof ReporterSupportsVideo) {
            ((ReporterSupportsVideo) reporter).videoRecording(videoOn, "allure-recordings");
            log.info("Enabled Video recordings feature");
        }

        if (screenshotOn && reporter instanceof ReporterSupportsScreenshot) {
            ((ReporterSupportsScreenshot) reporter).screenshotRecording(screenshotOn);
            log.info("Enabled Screenshots feature");
        }

        AtomicBoolean globalFailures = new AtomicBoolean(false);

        suitesMap.keySet().stream().forEach((String name) -> {
            try {
                if (!executeSuite.contentEquals("ALL") &&
                        !executeSuite.contentEquals(name)) {
                    log.warn("Suite {} not selected for execution (selected: {})", name, executeSuite);
                } else {
                    final Suite suite = suitesMap.get(name);

                    try {
                        reporter.startOutput(reportDirectory, name);
                        final boolean suiteFail = executeTestSuite(suite);
                        globalFailures.compareAndSet(false, suiteFail);
                    } catch (IOException ioe) {
                        globalFailures.set(true);
                        throw new ReportException("IO Error writing report file : " + ioe.getMessage(), ioe);
                    } finally {
                        controller.quit();
                    }
                }
            } catch (ReportException rpe) {
                log.error("Failed to initialize, check reports subsystem {}", rpe.getMessage(), rpe);
            }
        });
        return globalFailures.get();
    }

    public boolean executeTestSuite(Suite suite) throws ReportException {
        log.info("--------------");
        log.info("SUITE START: {}", suite.getName());
        log.trace("Starting suite reporting...");
        reporter.startTestSuite(suite);
        log.trace("About to execute suite tests: {}", suite.getTests().size());

        List<Test> copy = Lists.newArrayList(suite.getTests());
        copy.sort(Comparator.comparingInt(Test::getPriority));
        copy.forEach(test -> {
            executeTest(suite, test);
        });

        log.trace("Setting stderr and stdout to suite");
        reporter.setSystemError(suite.getErr().toString());
        reporter.setSystemOutput(suite.getOut().toString());
        log.trace("Ending suite reporting...");
        final boolean fail = reporter.endTestSuite(suite);
        log.trace("Suite reporting has completed");
        log.info("SUITE END: {}", suite.getName());

        return fail;
    }

    private void executeTest(Suite suite, Test test) {
        AtomicBoolean globalSkip = new AtomicBoolean(false);

        log.info("--------------");

        //sort by ordering
        List<TestMethod> testsToRun = Lists.newArrayList(test.getTestMethods()).stream().filter(testMethod -> !testMethod.isSkip()).collect(Collectors.toList());

        List<TestMethod> skippedTests = Lists.newArrayList(test.getTestMethods());
        skippedTests.removeAll(testsToRun);
        skippedTests.forEach(testMethod -> {
            reporter.addSkipped(test, testMethod, "Skipped due to Annotation configuration");
            testMethod.setSkip(true);
        });
        log.info("Test: {} - steps: {}, skipped: {}", test.getTestBean().getClass().getName(), testsToRun.size(), skippedTests.size());


        testsToRun.sort(Comparator.comparingInt(TestMethod::getOrdering));

        //before testmethod:
        test.getTestBeforeMethods().sort(Comparator.comparingInt(TestMethod::getOrdering));//sort beforeTest via order
        test.getTestBeforeMethods().forEach(tm -> {
            log.info("---- BEFORE START: {}", tm.getTestMethod());
            TestResult result = executeRecordingResult(suite, test, tm, false);
            if (result.notPassed()) { // if before is skipped, execute no other method - all are skipped.
                globalSkip.set(true);
                tm.getStdOut().append(result.getStdOutput());
                reporter.startTest(test, tm);
                reporter.addFailure(test, tm, result.getThrowable());
                reporter.endTest(test, tm);
            }
            log.info("---- BEFORE END: {}", tm.getTestMethod());
        });


        testsToRun.forEach(testMethod -> {
            AtomicBoolean localSkip = new AtomicBoolean(false);
            reporter.startTest(test, testMethod);


            try {
                if (globalSkip.get()) {
                    reporter.addSkipped(test, testMethod, "Skipped due to @AnaxBefore failure");
                    testMethod.setSkip(true);
                } else {
                    log.info("---- STEP START: {} ---", testMethod.getTestMethod());

                    //precondition:
                    testMethod.getPreconditions().forEach(tp -> {
                        log.info("---- PRECON START: {}", tp.getTestMethod());
                        executePrePost(suite, test, testMethod, localSkip, tp);
                        log.info("---- PRECON END: {}", tp.getTestMethod());
                    });

                    //execute method!
                    if (localSkip.get() == false) {
                        TestResult result = executeRecordingResult(suite, test, testMethod, true);
                        testMethod.getStdErr().append(result.getStdError());
                        testMethod.getStdOut().append(result.getStdOutput());
                        if (result.notPassed()) {
                            localSkip.set(true);
                            if (result.isInError()) {
                                reporter.addError(test, testMethod, result.getThrowable());
                            } else if (result.isFailed()) {
                                reporter.addFailure(test, testMethod, result.getThrowable());
                            }
                            testMethod.setPassed(false);
                        } else {
                            testMethod.setPassed(true);
                        }
                    } else {
                        testMethod.setSkip(true);
                    }
                    //postcondition:
                    testMethod.getPostconditions().forEach(tp -> {
                        log.info("---- POSTCON START: {}", tp.getTestMethod());
                        executePrePost(suite, test, testMethod, localSkip, tp);
                        log.info("---- POSTCON END: {}", tp.getTestMethod());
                    });


                }
            } finally {
                reporter.endTest(test, testMethod);

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

    private void executePrePost(Suite suite, Test test, TestMethod testMethod, AtomicBoolean localSkip, TestMethod tp) {
        if (localSkip.get() == false) {
            TestResult result = executeRecordingResult(suite, test, tp, false);
            testMethod.getStdErr().append(result.getStdError());
            testMethod.getStdOut().append(result.getStdOutput());
            if (result.notPassed()) {
                localSkip.set(true);
            }
        }
    }

    private TestResult executeRecordingResult(Suite suite, Test test, TestMethod tm, boolean isTest) {

        TestResult result = new TestResult();
        // capture stream for out
        ByteArrayOutputStream storedOut = new ByteArrayOutputStream();
        ByteArrayOutputStream storedErr = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        System.setOut(new PrintStream(new DuplicatingOutputStream(originalOut, storedOut)));
        System.setErr(new PrintStream(new DuplicatingOutputStream(originalErr, storedErr)));
        log.trace("Replaced out and error streams with recording versions");
        //execute method
        log.debug("About to execute {} ", tm.getTestMethod());

        long execTime = 0;
        try {
            long t0 = System.currentTimeMillis();
            if(tm.getDataproviderValue()==null) {
                tm.getTestMethod().invoke(test.getTestBean());
            }
            else {
                if(tm.getDataproviderValue()==null){
                    tm.getTestMethod().invoke(test.getTestBean());
                }
                else{
                    tm.getTestMethod().invoke(test.getTestBean(),tm.getDataproviderValue());
                }

            }



//            tm.getTestMethod().invoke(test.getTestBean());
            long t1 = System.currentTimeMillis();
            //if we're here, this was executed.
            execTime = t1 - t0;
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
            suitesMap.put(name, suite);
            return suite;
        }


    }

    public Test registerTest(Object bean, String beanDescription, String beanName, int priority, List<Suite> rgSuites) {

        Test test = Test.builder().testBean(bean).testBeanDescription(beanDescription).testBeanName(beanName).priority(priority).build();
        for (Suite s : rgSuites) {
            if (!suitesMap.containsKey(s.getName())) {
                suitesMap.put(s.getName(), s);
            }
            s.getTests().add(test);
        }
        return test;
    }

    public void registerBeforeTest(Test test, Method method, int ordering) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).ordering(ordering).build();
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


//    public TestMethod registerTestMethod(Test test, Method method, int ordering, boolean skip) {
//        TestMethod testMethod = TestMethod.builder().testMethod(method).ordering(ordering).skip(skip).build();
//        test.getTestMethods().add(testMethod);
//        return testMethod;
//    }

    public TestMethod registerTestMethod(Test test, Method method, String description, int ordering, boolean skip, Object dataproviderValue) {
        TestMethod testMethod = TestMethod.builder().testMethod(method).description(description).ordering(ordering).skip(skip).dataproviderValue(dataproviderValue).build();
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
            firstStream.write(b, off, len);
            secondStream.write(b, off, len);
        }
    }
}
