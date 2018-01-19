package org.anax.framework.reporting;


/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


import lombok.extern.slf4j.Slf4j;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.env.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.StreamSupport;

@Slf4j
public class DefaultJUnitReporter implements XMLConstants, AnaxTestReporter {

    private static final double ONE_SECOND = 1000.0;

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";


    private final Environment environment;
    private final WebController controller;

    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception exc) {
            throw new ExceptionInInitializerError(exc);
        }
    }

    /**
     * The XML document.
     */
    private Document doc;
    /**
     * The wrapper for the whole testsuite.
     */
    private Element rootElement;
    /**
     * Element for the current test.
     *
     * The keying of this map is a bit of a hack: tests are keyed by caseName(className) since
     * the Test we get for Test-start isn't the same as the Test we get during test-assumption-fail,
     * so we can't easily match Test objects without manually iterating over all keys and checking
     * individual fields.
     */
    private HashMap<String, Element> testElements = new HashMap<String, Element>();
    /**
     * tests that failed.
     */
    private HashMap failedTests = new HashMap();
    /**
     * Tests that were skipped.
     */
    private HashMap<String, Test> skippedTests = new HashMap<String, Test>();
    /**
     * Tests that were ignored. See the note above about the key being a bit of a hack.
     */
    private HashMap<String, Test> ignoredTests = new HashMap<String, Test>();
    /**
     * Timing helper.
     */
    private HashMap<String, Long> testStarts = new HashMap<String, Long>();
    /**
     * Where to write the log to.
     */
    private OutputStream out;

    private String reportScreenshotDirectory;

    public DefaultJUnitReporter(Environment environment, WebController controller, String reportScreenshotDirectory) {
        this.environment = environment;
        this.controller = controller;
        this.reportScreenshotDirectory = reportScreenshotDirectory;
    }


    @Override
    public void setOutput(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setSystemOutput(String out) {
        formatOutput(rootElement, SYSTEM_OUT, out);
    }

    @Override
    public void setSystemError(String out) {
        formatOutput(rootElement, SYSTEM_ERR, out);
    }

    /**
     * The whole testsuite started.
     * @param suite the testsuite.
     */
    @Override
    public void startTestSuite(Suite suite) throws ReportException {

        if (out == null) {
            throw new ReportException("Cannot start test suite without output stream configured");
        }

        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        String n = suite.getName();
        rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

        //add the timestamp
        LocalDateTime dateTime = LocalDateTime.now();
        //2014-01-21T16:17:18
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        //String text = dateTime.format(formatter);
        //rootElement.setAttribute(TIMESTAMP, text);
        //and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);

        if (environment instanceof StandardEnvironment) {

            MutablePropertySources propSrcs = ((AbstractEnvironment) environment).getPropertySources();
            StreamSupport.stream(propSrcs.spliterator(), false)
                    .filter(ps -> ps instanceof EnumerablePropertySource)
                    .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                    .flatMap(Arrays::<String>stream)
                    .forEach(propName ->  {
                        Element prop = doc.createElement(PROPERTY);
                        prop.setAttribute("name", propName);
                        prop.setAttribute("value", environment.getProperty(propName));
                        propsElement.appendChild(prop);
                    });
        }

    }

    /**
     * get the local hostname
     * @return the name of the local host, or "localhost" if we cannot work it out
     */
    private String getHostname()  {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * The whole testsuite ended.
     * @param suite the testsuite.
     * @throws
     */
    @Override
    public void endTestSuite(Suite suite) throws ReportException {
        rootElement.setAttribute(ATTR_TESTS, "" + suite.getExecutedTests());
        rootElement.setAttribute(ATTR_FAILURES, "" + suite.getFailedTests());
        rootElement.setAttribute(ATTR_ERRORS, "" + suite.getErroredTests());
        rootElement.setAttribute(ATTR_SKIPPED, "" + suite.getSkippedTests());
        rootElement.setAttribute(ATTR_TIME, "" + (suite.getTotalRunTime() / ONE_SECOND));
        if (out != null) {
            Writer wri = null;
            try {
                log.trace("Writing report to stream {}",out);
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                DOMSource source = new DOMSource(rootElement);

                BufferedOutputStream bos = new BufferedOutputStream(out);
                StreamResult result = new StreamResult(bos);
                transformer.transform(source, result);
                log.trace("Report: report writing completed");
            } catch (IOException exc) {
                throw new ReportException("Unable to write report, error = "+exc.getMessage(), exc);
            } catch (TransformerException e) {
                throw new ReportException("Unable to write report", e);
            } finally {
                if (wri != null) {
                    try {
                        wri.flush();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
                if (out != System.out && out != System.err) {
                    IOUtils.closeQuietly(wri);
                }
            }
        }
    }



    @Override
    public void startTest(Test test, TestMethod testMethod) {
        testStarts.put(createTestDescription(test,testMethod), System.currentTimeMillis());
    }

    private String createTestDescription(Test test, TestMethod method) {
        return createTestClass(test) + " - " + method.getTestMethod().getName();

    }

    private String createTestClass(Test test) {
        final String fullName = test.getTestBean().getClass().getName();
//        if (fullName.contains(".")) {
//            return fullName.substring(fullName.lastIndexOf(".")+1);
//        } else
            return fullName;
    }
    /**
     * Interface TestListener.
     *
     * <p>A Test is finished.
     * @param test the test.
     * @param testMethod
     */
    @Override
    public void endTest(Test test, TestMethod testMethod) {
        String testDescription = createTestDescription(test,testMethod);

        if (!testStarts.containsKey(testDescription)) {
            startTest(test, testMethod);
        }
        Element currentTest;
        boolean createdNow = false;
        if (!failedTests.containsKey(testDescription) && !skippedTests.containsKey(testDescription) && !ignoredTests.containsKey(testDescription)) {
            currentTest = doc.createElement(TESTCASE);
            currentTest.setAttribute(ATTR_NAME, test.getTestBean().getClass().getName()+"."+testMethod.getTestMethod().getName()+"()");
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME, test.getTestBean().getClass().getName());
            rootElement.appendChild(currentTest);
            testElements.put(testDescription, currentTest);
            createdNow = true;
        } else {
            currentTest = testElements.get(testDescription);
        }

        Long l = testStarts.get(testDescription);
        currentTest.setAttribute(ATTR_TIME,
                "" + ((System.currentTimeMillis() - l) / ONE_SECOND));

        if (!createdNow || testMethod.isPassed()) {
            formatOutput(currentTest, SYSTEM_ERR, testMethod.getStdErr().toString());
            formatOutput(currentTest, SYSTEM_OUT, testMethod.getStdOut().toString());
        }
    }


    @Override
    public void addSkipped(Test test,  TestMethod method, String skipReason) {
        formatSkip(test, method, skipReason);
        if (test != null) {
            ignoredTests.put(createTestDescription(test,method), test);
        }
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        formatProblems(FAILURE, test, method, t);
    }

    @Override
    public void addError(Test test,  TestMethod testMethod, Throwable t) {
        formatProblems(ERROR, test, testMethod, t);
    }

    private void formatProblems(String type, Test test,  TestMethod testMethod, Throwable t) {
        if (test != null) {
            endTest(test, testMethod);
            failedTests.put(createTestDescription(test,testMethod), test);
        }

        Element nested = doc.createElement(type);
        Element currentTest;
        if (test != null) {
            currentTest = testElements.get(createTestDescription(test,testMethod));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        Throwable actual = t;
        String message = actual.getMessage();
        while (message == null && actual.getCause() != null) {
            actual = actual.getCause();
            message = actual.getMessage();
        }
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, message);
        }
        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        //take screenshot here
        takeScreenshot(test,testMethod);
        //TODO figure what to do with it...

        String strace = getFilteredTrace(t);
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    private void takeScreenshot(Test test, TestMethod testMethod) {

        try {
            File screenShotFile = controller.takeScreenShot();
            File destScreenshotFile = new File(reportScreenshotDirectory, test.getTestBean().getClass().getName()+"."+testMethod.getTestMethod().getName()+".png");

            if (!destScreenshotFile.getParentFile().exists()) {
                destScreenshotFile.getParentFile().mkdirs();
            }
            Files.move(screenShotFile.toPath(),destScreenshotFile.toPath());


        } catch (IOException ioe ) {
            log.info("Failed to create screenshot : "+ ioe.getMessage());
        }
    }


    public static String getFilteredTrace(Throwable t) {
        final StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        t.printStackTrace(writer);
        return out.toString();
    }

    private static final String deAnsify(String text) {
        return text.replaceAll("(\\x1b\\x5b|\\x9b)[\\x30-\\x3f]*[\\x20-\\x2f]*[\\x40-\\x7e]", "");
    }

    private void formatOutput(Element el, String type, String output) {
        Element nested = doc.createElement(type);
        el.appendChild(nested);
        nested.appendChild(doc.createCDATASection(deAnsify(output)));
    }


    private void formatSkip(Test test, TestMethod testMethod, String message) {
        if (test != null) {
            endTest(test, testMethod);
        }

        Element nested = doc.createElement("skipped");

        if (message != null) {
            nested.setAttribute("message", message);
        }

        Element currentTest;
        if (test != null) {
            currentTest = testElements.get(createTestDescription(test, testMethod));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

    }
}

interface XMLConstants {
    /** the testsuites element for the aggregate document */
    String TESTSUITES = "testsuites";

    /** the testsuite element */
    String TESTSUITE = "testsuite";

    /** the testcase element */
    String TESTCASE = "testcase";

    /** the error element */
    String ERROR = "error";

    /** the failure element */
    String FAILURE = "failure";

    /** the system-err element */
    String SYSTEM_ERR = "system-err";

    /** the system-out element */
    String SYSTEM_OUT = "system-out";

    /** package attribute for the aggregate document */
    String ATTR_PACKAGE = "package";

    /** name attribute for property, testcase and testsuite elements */
    String ATTR_NAME = "name";

    /** time attribute for testcase and testsuite elements */
    String ATTR_TIME = "time";

    /** errors attribute for testsuite elements */
    String ATTR_ERRORS = "errors";

    /** failures attribute for testsuite elements */
    String ATTR_FAILURES = "failures";

    /** tests attribute for testsuite elements */
    String ATTR_TESTS = "tests";

    String ATTR_SKIPPED = "skipped";

    /** type attribute for failure and error elements */
    String ATTR_TYPE = "type";

    /** message attribute for failure elements */
    String ATTR_MESSAGE = "message";

    /** the properties element */
    String PROPERTIES = "properties";

    /** the property element */
    String PROPERTY = "property";

    /** value attribute for property elements */
    String ATTR_VALUE = "value";

    /** classname attribute for testcase elements */
    String ATTR_CLASSNAME = "classname";

    /** id attribute */
    String ATTR_ID = "id";

    /**
     * timestamp of test cases
     */
    String TIMESTAMP = "timestamp";

    /**
     * name of host running the tests
     */
    String HOSTNAME = "hostname";
}