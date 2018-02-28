package org.anax.framework.reporting;

import atu.testrecorder.ATUTestRecorder;
import atu.testrecorder.exceptions.ATUTestRecorderException;
import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.Severity;
import io.qameta.allure.model.*;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import static io.qameta.allure.util.ResultsUtils.getHostName;
import static io.qameta.allure.util.ResultsUtils.getThreadName;
import static org.mockito.Mockito.mock;


@Service("defaultJUnitTestReporter")
@Slf4j
public class AnaxAllureReporter implements AnaxTestReporter {

    private static  ATUTestRecorder recorder;
    private String recordingName;
    private final AllureLifecycle lifecycle;
    private String suiteName;

    @Autowired
    WebController controller;

    public AnaxAllureReporter(WebController controller, AllureLifecycle lifecycle) {
        this.controller = controller;
        this.lifecycle = lifecycle;
    }

    public AnaxAllureReporter() {
        this.lifecycle = Allure.getLifecycle();
    }

    public AllureLifecycle getLifecycle() {
        return lifecycle;
    }



    @Override
    public void setOutput(OutputStream outputStream) {

    }

    @Override
    public void setSystemOutput(String s) {

    }

    @Override
    public void setSystemError(String s) {

    }

    @Override
    public void startTestSuite(Suite suite) throws ReportException {
        suiteName = suite.getName();
        try{
            FileUtils.cleanDirectory(new File("allure-results"));
            log.info("Remove files under reports");
        }catch(Exception e){
            log.info("Files did not removed: "+e.getMessage());
        }
    }

    @Override
    public void endTestSuite(Suite suite) throws ReportException {
        try{
            String command = "allure generate allure-results --clean";
            Process process = Runtime.getRuntime().exec(command);
            log.info("Generate and open allure results");
            process.waitFor();
            Runtime.getRuntime().exec("allure open");
            log.info("Open results");
        }catch(Exception e){
            log.info("Report not generated: "+e.getMessage());
        }
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        String Test_UUID = getUniqueUuid(test,testMethod);
        final TestResult result = createTestResult(test, testMethod);
        getLifecycle().scheduleTestCase(Test_UUID, result);
        getLifecycle().startTestCase(Test_UUID);

        recordingName = "test"+ UUID.randomUUID();
        try {
            recorder = new ATUTestRecorder(getPath(),recordingName,false);
        } catch (ATUTestRecorderException e) {
            e.printStackTrace();
        }
        try {
            recorder.start();
        } catch (ATUTestRecorderException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        String Test_UUID = getUniqueUuid(test,testMethod);
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStep(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setSeverity(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setIssue(testMethod));
        getLifecycle().updateTestCase(Test_UUID, setStatus(getStepStatus(testMethod)));
        getLifecycle().updateTestCase(Test_UUID,setRecording());
        try {

            recorder.stop();
        } catch (ATUTestRecorderException e) {
            e.printStackTrace();
        }

        getLifecycle().stopTestCase(Test_UUID);
        getLifecycle().writeTestCase(Test_UUID);
    }

    @Override
    public void addFailure(Test test, TestMethod testMethod, Throwable throwable) {
        Throwable actual = throwable;
        String message = actual.getMessage();
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.FAILED,throwable, testMethod));
        try {
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSkipped(Test test, TestMethod testMethod, String skipReason) {
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.SKIPPED,skipReason));
        try {
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addError(Test test, TestMethod testMethod, Throwable throwable) {
        Throwable actual = throwable;
        String message = actual.getMessage();
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.FAILED,throwable,testMethod));
        try {
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Consumer<TestResult> setStatus(final Status status) {

        return result -> {
            result.withStatus(status);
        };

    }

    private Consumer<TestResult> setStatus(final Status status, Throwable throwable, TestMethod method) {
        return result -> {
            result.withStatus(status);
            StatusDetails det = new StatusDetails();

            if (throwable!=null) {
                det.setMessage(throwable.getMessage()); //TODO find the correct message
                result.withStatusDetails(det);
                StringWriter wr = new StringWriter();
                throwable.printStackTrace(new PrintWriter(wr));


                StringBuilder html = new StringBuilder();

                html.append("<h3>Console Logs</h3>");
                html.append("<pre>" + method.getStdOut().toString() + "</pre>");


                html.append("<h3>Exception Detail</h3>");
                html.append("<pre>" + wr.toString() + "</pre>");

                result.withDescriptionHtml(html.toString());
            }
        };
    }

    private Consumer<TestResult> setStatus(final Status status, String reason) {
        return result -> {
            result.withStatus(status);
            result.withDescription(reason);
        };
    }




    private Consumer<TestResult> setSeverity(final TestMethod testMethod) {
        return result -> {
            Severity severityAnnotationLevel = (Severity) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(Severity.class)).findFirst().orElse(null);
            if(severityAnnotationLevel !=null) {
                result.withLabels(
                        new Label().withName("severity").withValue(severityAnnotationLevel.value().toString())
                );
            }
        };
    }

    private Consumer<TestResult> setIssue(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            io.qameta.allure.Link issueAnnotationLink = (io.qameta.allure.Link) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Link.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.withLinks(
                        new Link().withName(issueAnnotationLink.value().toString())
                );
            }
        };
    }


    private TestResult createTestResult(Test test, TestMethod testMethod) {
        final String className = test.getTestBeanName();
        final String methodName = testMethod.getTestMethod().getName();
        final String name = Objects.nonNull(className+methodName) ? methodName : className;
        final String fullName = Objects.nonNull(methodName) ? String.format("%s.%s", className, methodName) : className;


        final TestResult testResult = new TestResult()
                .withUuid(getUniqueUuid(test,testMethod))
                .withHistoryId(getUniqueUuid(test,testMethod))
                .withName(name)
                .withFullName(fullName)
                .withLabels(
                        new Label().withName("package").withValue("mypackage"),
                        new Label().withName("testClass").withValue(className),
                        new Label().withName("testMethod").withValue(name),
                        new Label().withName("suite").withValue(suiteName),
                        new Label().withName("host").withValue(getHostName()),
                        new Label().withName("thread").withValue(getThreadName())
                );
        return testResult;
    }
    private String getUniqueUuid(Test test, TestMethod testMethod) {
        String id = test.getTestBeanName()+"."+testMethod.getTestMethod().getName();
        return id;
    }

    public byte[] takeScreenshotOnFailure() throws IOException {
        Allure.addAttachment("Screenshot",new ByteArrayInputStream(controller.takeScreenShotAsBytes()));
        return controller.takeScreenShotAsBytes();
    }


    private Consumer<TestResult> setStep(final TestMethod testMethod) {
        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            AnaxTestStep stepDescription = (AnaxTestStep) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(AnaxTestStep.class)).findFirst().orElse(null);
            if (stepDescription != null) {
                if (stepDescription.description().toString().isEmpty()) {
                    result.withSteps(
                            new StepResult().withName("No available description found.").withStatus(getStepStatus(testMethod))
                    );
                }
                else {
                    result.withSteps(
                            new StepResult().withName(stepDescription.description().toString()).withStatus(getStepStatus(testMethod))
                    );
                }

            }
        };
    }

    private Consumer<TestResult> setRecording() {
        return result -> {
            result.withAttachments(
                    new Attachment().withName("Recording").withSource(getPath()+"/"+recordingName+".mov").withType("mov")
            );
        };
    }

    private Status getStepStatus(TestMethod testMethod){
        if(testMethod.isPassed()){
            return Status.PASSED;
        }
        else if(testMethod.isSkip()){
            return Status.SKIPPED;
        }
        else{
            return Status.FAILED;
        }
    }

    private Supplier<InputStream> getStreamWithTimeout(final long sec) throws InterruptedException {
        TimeUnit.SECONDS.sleep(sec);
        return () -> mock(InputStream.class);
    }

    private String getPath(){
        return new File("reports").getAbsolutePath();
    }
}
