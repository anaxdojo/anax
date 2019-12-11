package org.anax.framework.reporting;

import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;

import java.io.FileNotFoundException;

/**
 * AnaxTest reporter standard interface for callbacks for plugins that generate reports. Default automatically enabled
 * reporter is a JUnit reporter that produces XML in Junit 4 format.
 *
 */
public interface AnaxTestReporter {

    void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException;

    void setSystemOutput(String out);

    void setSystemError(String out);

    void startTestSuite(Suite suite) throws ReportException;

    boolean endTestSuite(Suite suite) throws ReportException;

    void startAnaxTest(Test test);

    void endAnaxTest(Test test);

    void startTest(Test test, TestMethod testMethod);

    void endTest(Test test, TestMethod testMethod);

    void addFailure(Test test, TestMethod method, Throwable t);

    void addSkipped(Test test, TestMethod method, String skipReason);

    void addError(Test test, TestMethod method, Throwable t);
}
