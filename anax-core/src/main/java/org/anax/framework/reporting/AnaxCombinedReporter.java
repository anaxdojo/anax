package org.anax.framework.reporting;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;

import java.io.FileNotFoundException;

/**
 * A reporter that implements {@link AnaxTestReporter} and allows two reporters to be used concurrently, for example
 * a Junit and Allure or an Allure and ZAPI or any other combination.
 * To use, add a configured bean as @Primary with the two reporters as constructor arguments.
 *
 */
@Slf4j
public class AnaxCombinedReporter implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo{

    AnaxTestReporter reporterA;
    AnaxTestReporter reporterB;

    public AnaxCombinedReporter(AnaxTestReporter reporterA, AnaxTestReporter reporterB) {
        this.reporterA = reporterA;
        this.reporterB = reporterB;
    }

    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {
        reporterA.startOutput(reportDirectory, suiteName);
        reporterB.startOutput(reportDirectory, suiteName);
    }

    @Override
    public void setSystemOutput(String out) {
        reporterA.setSystemOutput(out);
        reporterB.setSystemOutput(out);
    }

    @Override
    public void setSystemError(String out) {
        reporterA.setSystemError(out);
        reporterB.setSystemError(out);
    }

    @Override
    public void startTestSuite(Suite suite) throws ReportException {
        reporterA.startTestSuite(suite);
        reporterB.startTestSuite(suite);
    }

    @Override
    public boolean endTestSuite(Suite suite) throws ReportException {
        return (reporterA.endTestSuite(suite) && reporterB.endTestSuite(suite));
    }

    @Override
    public void startAnaxTest(Test test) {
        reporterA.startAnaxTest(test);
        reporterB.startAnaxTest(test);
    }

    @Override
    public void endAnaxTest(Test test) {
        reporterA.endAnaxTest(test);
        reporterB.endAnaxTest(test);
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        reporterA.startTest(test, testMethod);
        reporterB.startTest(test, testMethod);
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        reporterA.endTest(test, testMethod);
        reporterB.endTest(test, testMethod);
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        reporterA.addFailure(test, method, t);
        reporterB.addFailure(test, method, t);
    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {
        reporterA.addSkipped(test, method, skipReason);
        reporterB.addSkipped(test, method, skipReason);
    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t) {
        reporterA.addError(test, method, t);
        reporterB.addError(test, method, t);
    }

    @Override
    public void screenshotRecording(boolean enable) {
        if(reporterA instanceof ReporterSupportsScreenshot){
            ((ReporterSupportsScreenshot) reporterA).screenshotRecording(enable);
        }
        if(reporterB instanceof ReporterSupportsScreenshot){
            ((ReporterSupportsScreenshot) reporterB).screenshotRecording(enable);
        }
    }

    @Override
    public void videoRecording(boolean enable, String videoBaseDirectory) {
        if(reporterA instanceof ReporterSupportsVideo){
            ((ReporterSupportsVideo) reporterA).videoRecording(enable,videoBaseDirectory);
        }
        if(reporterB instanceof ReporterSupportsVideo){
            ((ReporterSupportsVideo) reporterB).videoRecording(enable,videoBaseDirectory);
        }
    }
}
