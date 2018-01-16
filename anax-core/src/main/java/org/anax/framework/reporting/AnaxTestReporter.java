package org.anax.framework.reporting;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;

import java.io.OutputStream;

public interface AnaxTestReporter {
    void setOutput(OutputStream out);

    void setSystemOutput(String out);

    void setSystemError(String out);

    void startTestSuite(Suite suite) throws ReportException;

    void endTestSuite(Suite suite) throws ReportException;

    void startTest(Test t);

    void endTest(Test test);

    void addFailure(Test test, TestMethod method, Throwable t);

    void addSkipped(Test test, TestMethod method, String skipReason);

    void addError(Test test, TestMethod method, Throwable t);
}
