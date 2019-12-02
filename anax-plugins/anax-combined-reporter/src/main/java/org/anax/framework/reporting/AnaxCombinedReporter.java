package org.anax.framework.reporting;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.FileNotFoundException;

@Slf4j
public class AnaxCombinedReporter implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo{

    public AnaxCombinedReporter(AnaxTestReporter reporterA, AnaxTestReporter reporterB) {
        this.allureReporter = reporterA;
        this.zapiReporter = reporterB;
    }

    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {
        allureReporter.startOutput(reportDirectory, suiteName);
        zapiReporter.startOutput(reportDirectory, suiteName);
    }

    @Override
    public void setSystemOutput(String out) {
        allureReporter.setSystemOutput(out);
        zapiReporter.setSystemOutput(out);
    }

    @Override
    public void setSystemError(String out) {
        allureReporter.setSystemError(out);
        zapiReporter.setSystemError(out);
    }

    @Override
    public void startTestSuite(Suite suite) throws ReportException {
        allureReporter.startTestSuite(suite);
        zapiReporter.startTestSuite(suite);
    }

    @Override
    public boolean endTestSuite(Suite suite) throws ReportException {
        return (allureReporter.endTestSuite(suite) && zapiReporter.endTestSuite(suite));
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        allureReporter.startTest(test, testMethod);
        zapiReporter.startTest(test, testMethod);
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        allureReporter.endTest(test, testMethod);
        zapiReporter.endTest(test, testMethod);
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        allureReporter.addFailure(test, method, t);
        zapiReporter.addFailure(test, method, t);
    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {
        allureReporter.addSkipped(test, method, skipReason);
        zapiReporter.addSkipped(test, method, skipReason);
    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t) {
        allureReporter.addError(test, method, t);
        zapiReporter.addError(test, method, t);
    }

    AnaxTestReporter allureReporter;
    AnaxTestReporter zapiReporter;

    @Override
    public void screenshotRecording(boolean enable) {
        if(allureReporter instanceof ReporterSupportsScreenshot){
            ((ReporterSupportsScreenshot) allureReporter).screenshotRecording(enable);
        }
        if(zapiReporter instanceof ReporterSupportsScreenshot){
            ((ReporterSupportsScreenshot) zapiReporter).screenshotRecording(enable);
        }
    }

    @Override
    public void videoRecording(boolean enable, String videoBaseDirectory) {
        if(allureReporter instanceof ReporterSupportsVideo){
            ((ReporterSupportsVideo) allureReporter).videoRecording(enable,"allure-results");
        }
        if(zapiReporter instanceof ReporterSupportsVideo){
            ((ReporterSupportsVideo) zapiReporter).videoRecording(enable,"allure-results");
        }
    }
}
