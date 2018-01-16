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


import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;


public class DefaultJUnitReporter implements XMLConstants, AnaxTestReporter {

    private static final double ONE_SECOND = 1000.0;

    /** constant for unnnamed testsuites/cases */
    private static final String UNKNOWN = "unknown";

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




    @Override
    public void setOutput(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setSystemOutput(String out) {
        formatOutput(SYSTEM_OUT, out);
    }

    @Override
    public void setSystemError(String out) {
        formatOutput(SYSTEM_ERR, out);
    }

    /**
     * The whole testsuite started.
     * @param suite the testsuite.
     */
    @Override
    public void startTestSuite(Suite suite) {
        doc = getDocumentBuilder().newDocument();
        rootElement = doc.createElement(TESTSUITE);
        String n = suite.getName();
        rootElement.setAttribute(ATTR_NAME, n == null ? UNKNOWN : n);

        //add the timestamp
        final String timestamp = DateTimeFormatter.ISO_DATE_TIME.format(new Date().toInstant());
        rootElement.setAttribute(TIMESTAMP, timestamp);
        //and the hostname.
        rootElement.setAttribute(HOSTNAME, getHostname());

        // Output properties
        Element propsElement = doc.createElement(PROPERTIES);
        rootElement.appendChild(propsElement);

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
        rootElement.setAttribute(ATTR_TESTS, "" + suite.runCount());
        rootElement.setAttribute(ATTR_FAILURES, "" + suite.failureCount());
        rootElement.setAttribute(ATTR_ERRORS, "" + suite.errorCount());
        rootElement.setAttribute(ATTR_SKIPPED, "" + suite.skipCount());
        rootElement.setAttribute(
                ATTR_TIME, "" + (suite.getRunTime() / ONE_SECOND));
        if (out != null) {
            Writer wri = null;
            try {
                wri = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
                wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(rootElement);

                BufferedOutputStream bos = new BufferedOutputStream(out);
                StreamResult result = new StreamResult(bos);
                transformer.transform(source, result);

            } catch (IOException exc) {
                throw new ReportException("Unable to write log file", exc);
            } catch (TransformerException e) {
                e.printStackTrace();
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

    /**
     * Interface TestListener.
     *
     * <p>A new Test is started.
     * @param t the test.
     */
    @Override
    public void startTest(Test t) {
        testStarts.put(createDescription(t), System.currentTimeMillis());
    }

    private static String createDescription(Test test) {
        return test.getTestBean().getClass().getCanonicalName();

    }

    /**
     * Interface TestListener.
     *
     * <p>A Test is finished.
     * @param test the test.
     */
    @Override
    public void endTest(Test test) {
        String testDescription = createDescription(test);

        if (!testStarts.containsKey(testDescription)) {
            startTest(test);
        }
        Element currentTest;
        if (!failedTests.containsKey(test) && !skippedTests.containsKey(testDescription) && !ignoredTests.containsKey(testDescription)) {
            currentTest = doc.createElement(TESTCASE);
            String n = createDescription(test);
            currentTest.setAttribute(ATTR_NAME,
                    n == null ? UNKNOWN : n);
            // a TestSuite can contain Tests from multiple classes,
            // even tests with the same name - disambiguate them.
            currentTest.setAttribute(ATTR_CLASSNAME,
                    createDescription(test));
            rootElement.appendChild(currentTest);
            testElements.put(createDescription(test), currentTest);
        } else {
            currentTest = testElements.get(testDescription);
        }

        Long l = testStarts.get(createDescription(test));
        currentTest.setAttribute(ATTR_TIME,
                "" + ((System.currentTimeMillis() - l) / ONE_SECOND));
    }

    /**
     * Interface TestListener for JUnit &lt;= 3.4.
     *
     * <p>A Test failed.
     * @param test the test.
     * @param t the exception.
     */
    @Override
    public void addFailure(Test test, Throwable t) {
        formatError(FAILURE, test, t);
    }
    @Override
    public void addSkipped(Test test, String skipReason) {
        formatSkip(test, skipReason);
        if (test != null) {
            ignoredTests.put(createDescription(test), test);
        }
    }




    /**
     * Interface TestListener.
     *
     * <p>An error occurred while running the test.
     * @param test the test.
     * @param t the error.
     */
    @Override
    public void addError(Test test, Throwable t) {
        formatError(ERROR, test, t);
    }

    private void formatError(String type, Test test, Throwable t) {
        if (test != null) {
            endTest(test);
            failedTests.put(test, test);
        }

        Element nested = doc.createElement(type);
        Element currentTest;
        if (test != null) {
            currentTest = testElements.get(createDescription(test));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

        String message = t.getMessage();
        if (message != null && message.length() > 0) {
            nested.setAttribute(ATTR_MESSAGE, t.getMessage());
        }
        nested.setAttribute(ATTR_TYPE, t.getClass().getName());

        String strace = getFilteredTrace(t);
        Text trace = doc.createTextNode(strace);
        nested.appendChild(trace);
    }

    public static String getFilteredTrace(Throwable t) {
        final StringWriter out = new StringWriter();
        PrintWriter writer = new PrintWriter(out);
        t.printStackTrace(writer);
        return out.toString();
    }



    private void formatOutput(String type, String output) {
        Element nested = doc.createElement(type);
        rootElement.appendChild(nested);
        nested.appendChild(doc.createCDATASection(output));
    }


    public void formatSkip(Test test, String message) {
        if (test != null) {
            endTest(test);
        }

        Element nested = doc.createElement("skipped");

        if (message != null) {
            nested.setAttribute("message", message);
        }

        Element currentTest;
        if (test != null) {
            currentTest = testElements.get(createDescription(test));
        } else {
            currentTest = rootElement;
        }

        currentTest.appendChild(nested);

    }

    public void testAssumptionFailure(Test test, Throwable failure) {
        formatSkip(test, failure.getMessage());
        skippedTests.put(createDescription(test), test);

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