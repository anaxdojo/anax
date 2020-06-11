package org.anax.framework.reporting;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;
import java.io.FileNotFoundException;


@Slf4j
@Component("customTestReporter")
@Controller
public class CustomReporter implements AnaxTestReporter {
    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {
        log.info("Websocket Reporting... ");
    }

    @Override
    public void setSystemOutput(String out) {
        log.info("Websocket Reporting... ");
    }

    @Override
    public void setSystemError(String out) {

    }

    @SendTo("/topic/testSuite")
    @Override
    public void startTestSuite(Suite suite) throws ReportException {
        log.info("Websocket Reporting... "+suite.getName());
    }

    @Override
    public boolean endTestSuite(Suite suite) throws ReportException {
        return false;
    }

    @Override
    public void startAnaxTest(Test test) {
        log.info("Websocket Reporting... Start Anax test");
    }

    @Override
    public void endAnaxTest(Test test) {
        log.info("Websocket Reporting... End Anax test");
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        log.info("Websocket Reporting... Start Test");
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        log.info("Websocket Reporting... End Test");
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
