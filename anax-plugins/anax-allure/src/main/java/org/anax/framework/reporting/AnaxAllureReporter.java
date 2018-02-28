package org.anax.framework.reporting;

import io.qameta.allure.*;
import io.qameta.allure.core.Configuration;
import io.qameta.allure.core.Plugin;
import io.qameta.allure.model.*;
import io.qameta.allure.model.Attachment;
import io.qameta.allure.model.Link;
import io.qameta.allure.plugin.DefaultPluginLoader;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.qameta.allure.util.ResultsUtils.getHostName;
import static io.qameta.allure.util.ResultsUtils.getThreadName;
import static org.mockito.Mockito.mock;

@Service("anaxTestReporter")
@Slf4j
public class AnaxAllureReporter implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo {

    private final AllureLifecycle lifecycle;
    private String suiteName;

    @Autowired
    WebController controller;
    private boolean screenshotEnable;
    private boolean videoEnable;
    private String videoBaseDirectory;
    private String reportDirectory;

    public AnaxAllureReporter() {
        this.lifecycle = Allure.getLifecycle();
    }

    public AllureLifecycle getLifecycle() {
        return lifecycle;
    }


    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {

        this.reportDirectory = reportDirectory;
        this.suiteName = suiteName;
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

            generate(new File(reportDirectory).toPath(), Arrays.asList(new Path[] { new File("allure-results").toPath()}), true);

//            String command = "allure generate allure-results --clean";
//            Process process = Runtime.getRuntime().exec(command);
//            log.info("Generate and open allure results");
//            process.waitFor();
//            Runtime.getRuntime().exec("allure open");
//            log.info("Open results");
        }catch(Exception e){
            log.info("Report not generated: "+e.getMessage());
        }
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        String testUniqueID = getUniqueUuid(test,testMethod);
        final TestResult result = createTestResult(test, testMethod);
        getLifecycle().scheduleTestCase(testUniqueID, result);
        getLifecycle().startTestCase(testUniqueID);

        if (videoEnable) {
//        if(video.equals("true")) {
//            try {
//                recorder = new ATUTestRecorder(getPath(), recordingName, false);
//            } catch (ATUTestRecorderException e) {
//                e.printStackTrace();
//            }
//            try {
//                recorder.start();
//            } catch (ATUTestRecorderException e) {
//                e.printStackTrace();
//            }
//        }
        } else {
            log.warn("Video recording feature disabled");
        }
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        String testUniqueID = getUniqueUuid(test,testMethod);

        if(videoEnable) {
//            try {
//                recorder.stop();
//            } catch (ATUTestRecorderException e) {
//                e.printStackTrace();
//            }
//            getLifecycle().updateTestCase(testUniqueID, setRecording());
        }

        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStep(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setSeverity(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setIssue(testMethod));
        getLifecycle().updateTestCase(testUniqueID, setStatus(getStepStatus(testMethod)));
        getLifecycle().updateTestCase(testUniqueID, setPassStdOut(testMethod));

        getLifecycle().stopTestCase(testUniqueID);
        getLifecycle().writeTestCase(testUniqueID);
    }

    @Override
    public void addFailure(Test test, TestMethod testMethod, Throwable throwable) {
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

            StringBuilder html = new StringBuilder();
            if (throwable!=null) {
                det.setMessage(throwable.getMessage()); //TODO find the correct message
                result.withStatusDetails(det);
                StringWriter wr = new StringWriter();
                throwable.printStackTrace(new PrintWriter(wr));

                html.append("<h3>Console Logs</h3>");
                html.append("<pre>" + method.getStdOut().toString() + "</pre>");


                html.append("<h3>Exception Detail</h3>");
                html.append("<pre>" + wr.toString() + "</pre>");
                result.withDescriptionHtml(html.toString());
            }
        };
    }

    private Consumer<TestResult> setPassStdOut(TestMethod method) {
        return result -> {
            StringBuilder html = new StringBuilder();

            html.append("<h3>Console Logs</h3>");
            html.append("<pre>" + method.getStdOut().toString() + "</pre>");
            result.withDescriptionHtml(html.toString());
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
        final String name = Objects.nonNull(methodName) ? className+"_"+methodName : className;
        final String fullName = Objects.nonNull(methodName) ? String.format("%s.%s", className, methodName) : className;


        final TestResult testResult = new TestResult()
                .withUuid(getUniqueUuid(test,testMethod))
                .withHistoryId(getUniqueUuid(test,testMethod))
                .withName(name)
                .withFullName(fullName)
                .withLabels(
                        new Label().withName("package").withValue(test.getClass().getPackage().toString()),
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

    private void takeScreenshotOnFailure() throws IOException {
        if (screenshotEnable) {
            Allure.addAttachment("Screenshot", new ByteArrayInputStream(controller.takeScreenShotAsBytes()));
        } else {
            log.warn("Screenshot feature disabled");
        }
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

    private Consumer<TestResult> setRecording(String UUID) {
        return result -> {
            result.withAttachments(
                    new Attachment().withName("Recording").withSource(videoBaseDirectory+"/"+UUID+".mov").withType("mov")
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


    @Override
    public void screenshotRecording(boolean enable) {
        screenshotEnable = enable;
    }

    @Override
    public void videoRecording(boolean enable, String videoBaseDirectory) {
        this.videoEnable = enable;
        this.videoBaseDirectory = videoBaseDirectory;
    }

    public void generate(final Path reportDirectory,
                             final List<Path> resultsDirectories,
                             final boolean clean) {
        final boolean directoryExists = Files.exists(reportDirectory);
        if (clean && directoryExists) {
            FileUtils.deleteQuietly(reportDirectory.toFile());
        }

        try {
            ReportGenerator generator = new ReportGenerator(new ConfigurationBuilder()
                    .useDefault()
                    .build());
            generator.generate(reportDirectory, resultsDirectories);
        } catch (IOException e) {
            log.error("Could not generate report: {}", e);
        }
        log.info("Report successfully generated to {}", reportDirectory);
    }




}
