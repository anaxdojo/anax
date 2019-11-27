package org.anax.framework.integrations.reporting;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReportException;

import java.io.FileNotFoundException;

public class ZapiReporting implements AnaxTestReporter {
    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String out) {

    }

    @Override
    public void startTestSuite(Suite suite) throws ReportException {

    }

    @Override
    public boolean endTestSuite(Suite suite) throws ReportException {
        return false;
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {

    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {

    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {

    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {

    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t) {

    }
}
