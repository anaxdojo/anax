package org.anax.framework.configuration;

import com.google.common.collect.Lists;
import org.anax.framework.controllers.WebController;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.model.TestResult;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AnaxSequentialSuiteRunner extends AnaxDefaultSuiteRegistrar implements AnaxSuiteRunner {

    protected final AnaxTestReporter reporter;

    protected boolean shouldAlsoExecute = false;

    @Value("${anax.report.directory:reports/}")
    protected String reportDirectory;


    public AnaxSequentialSuiteRunner(@Autowired AnaxTestReporter reporter) {
        this.reporter = reporter;
    }

    @Value("${anax.exec.suite:ALL}")
    protected String executeSuite;

    @Autowired
    protected WebController controller;

    @PostConstruct
    public void postConstruct() {

        File dir = new File(reportDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

    }


    @Override
    public void createExecutionPlan(boolean executePlan) {
        shouldAlsoExecute = executePlan;

        if (shouldAlsoExecute) { //TODO handle the boolean for execution or display
            log.info("Executing test Suites: {}", suitesMap.keySet());
        } else {
            log.info("Planning for execution of Suites: {}", suitesMap.keySet());
        }
        suitesMap.keySet().stream().forEach( (String name) -> {
            try {
                if (!executeSuite.contentEquals("ALL") &&
                        !executeSuite.contentEquals(name)) {
                    log.warn("Suite {} not selected for execution (selected: {})", name, executeSuite);
                } else {
                    final Suite suite = suitesMap.get(name);

                    try (FileOutputStream outputStream = new FileOutputStream(new File(reportDirectory, createReportFilename(name)))) {
                        executeTestSuite(suite, outputStream);
                    } catch (IOException ioe) {
                        throw new ReportException("IO Error writing report file : " + ioe.getMessage(), ioe);
                    }
                }
            } catch (ReportException rpe) {
                log.error("Failed to initialize, check reports subsystem {}", rpe.getMessage(),rpe);
            } finally {
                //close the browser
                controller.quit();
            }
        });

    }

    protected String createReportFilename(String name) {
        return "junit-compat-report-"+normalizeFile(name)+"-"+ Long.toHexString(System.currentTimeMillis()) + ".xml";
    }

    protected String normalizeFile(String s) {
        char fileSep = File.pathSeparatorChar;
        char escape = '_'; // ... or some other legal char.
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (ch <= ' ' || ch >= 0x7F || ch == fileSep ||
                (ch == '.' && i == 0)
                    || ch == escape) {
                sb.append(escape);

            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    protected void executeTestSuite(Suite suite, OutputStream out) throws ReportException {
        log.info("--------------");
        log.info("SUITE START: {}", suite.getName());
        log.trace("Starting suite reporting...");
        reporter.setOutput(out);
        reporter.startTestSuite(suite);
        log.trace("About to execute suite tests: {}",suite.getTests().size());

        List<Test> copy = Lists.newArrayList(suite.getTests());
        copy.sort(Comparator.comparingInt(Test::getPriority));
        copy.forEach(test -> {
            executeTest(suite, test);
        });

        log.trace("Setting stderr and stdout to suite");
        reporter.setSystemError(suite.getErr().toString());
        reporter.setSystemOutput(suite.getOut().toString());
        log.trace("Ending suite reporting...");
        reporter.endTestSuite(suite);
        log.trace("Suite reporting has completed");
        log.info("SUITE END: {}", suite.getName());

    }

    protected void executeTest(Suite suite, Test test) {
        AtomicBoolean globalSkip = new AtomicBoolean(false);

        log.info("--------------");

        //sort by ordering
        List<TestMethod> testsToRun = Lists.newArrayList(test.getTestMethods()).stream().filter(testMethod -> !testMethod.isSkip()).collect(Collectors.toList());

        List<TestMethod> skippedTests = Lists.newArrayList( test.getTestMethods() );
        skippedTests.removeAll(testsToRun);
        skippedTests.forEach( testMethod -> {
            reporter.addSkipped(test, testMethod, "Skipped due to Annotation configuration");
        });
        log.info("Test: {} - steps: {}, skipped: {}", test.getTestBean().getClass().getName(), testsToRun.size(), skippedTests.size());


        testsToRun.sort(Comparator.comparingInt(TestMethod::getOrdering));

        //before testmethod:
        test.getTestBeforeMethods().forEach(tm -> {
            log.info("---- BEFORE START: {}", tm.getTestMethod());
            TestResult result = executeRecordingResult(suite, test, tm, false);
            if (result.notPassed()) { // if before is skipped, execute no other method - all are skipped.
                globalSkip.set(true);
            }
            log.info("---- BEFORE END: {}", tm.getTestMethod());
        });

        testsToRun.forEach(testMethod -> {
            AtomicBoolean localSkip = new AtomicBoolean(false);
            reporter.startTest(test, testMethod);


            try {
                if (globalSkip.get()) {
                    reporter.addSkipped(test, testMethod, "Skipped due to @AnaxBefore failure");
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
                                reporter.addFailure(test,testMethod, result.getThrowable());
                            }
                            testMethod.setPassed(false);
                        } else {
                            testMethod.setPassed(true);
                        }
                    }
                    //postcondition:
                    testMethod.getPostconditions().forEach(tp -> {
                        log.info("---- POSTCON START: {}", tp.getTestMethod());
                        executePrePost(suite, test, testMethod, localSkip,  tp);
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

    protected void executePrePost(Suite suite, Test test, TestMethod testMethod, AtomicBoolean localSkip, TestMethod tp) {
        if (localSkip.get() == false) {
            TestResult result = executeRecordingResult(suite, test, tp, false);
            testMethod.getStdErr().append(result.getStdError());
            testMethod.getStdOut().append(result.getStdOutput());
            if (result.notPassed()) {
                localSkip.set(true);
            }
        }
    }

    protected TestResult executeRecordingResult(Suite suite, Test test, TestMethod tm, boolean isTest) {

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
