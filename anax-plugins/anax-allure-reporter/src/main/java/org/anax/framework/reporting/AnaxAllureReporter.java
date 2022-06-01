package org.anax.framework.reporting;

import io.qameta.allure.*;
import io.qameta.allure.model.*;
import io.qameta.allure.model.Link;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.annotations.AnaxIssues;
import org.anax.framework.annotations.AnaxTestStep;
import org.anax.framework.capture.VideoMaker;
import org.anax.framework.controllers.VoidController;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.logging.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.qameta.allure.util.ResultsUtils.getHostName;
import static io.qameta.allure.util.ResultsUtils.getThreadName;

@Service("allureAnaxTestReporter")
@Slf4j
public class AnaxAllureReporter implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo {

    @Value("${anax.keepResults:false}") Boolean keepResults;
    @Value("${anax.allure.report.directory:allure-report/}") String reportAllureDirectory;
    @Value("${anax.allure.results.directory:allure-results/}") String resultsAllureDirectory;
    /** do not change the FPS value over 15, due to h/w limitations */
    @Value("${anax.video.fps:10}") Integer videoFramesPerSec;
    /** how many seconds to continue recording, after the "end recording" has been called */
    @Value("${anax.video.waitSecAtEnd:5}") Integer videoWaitSeconds;

    private final AllureLifecycle lifecycle;
    private String suiteName;
    private VideoMaker videoMaker;

    @Autowired
    protected WebController controller;

    private boolean screenshotEnable;
    private boolean videoEnable;
    private String videoBaseDirectory;
    private String reportDirectory;
    private Boolean failed = false;

    public AnaxAllureReporter() {
        this.lifecycle = Allure.getLifecycle();
    }

    public AllureLifecycle getLifecycle() {
        return lifecycle;
    }


    @Override
    public void startOutput(String reportDirectory, String suiteName){
        this.reportDirectory = reportDirectory.contentEquals("reports/")?reportAllureDirectory:reportDirectory;
        this.suiteName = suiteName;
        log.info("Allure output directory {} suite name {}",this.reportDirectory,suiteName);
    }

    @Override
    public void setSystemOutput(String s) {

    }

    @Override
    public void setSystemError(String s) {

    }

    @Override
    public void startTestSuite(Suite suite) {
        suiteName = suite.getName();
        if(!keepResults) {//In case you need to merge results for all many suites execution
            try {
                FileUtils.cleanDirectory(new File(resultsAllureDirectory));
                log.info("Remove files under reports");
            } catch (Exception e) {
                log.info("Files did not removed: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean endTestSuite(Suite suite){
        try{
            generate(new File(reportDirectory).toPath(), Arrays.asList(new File(resultsAllureDirectory).toPath()), true);
        }catch(Exception e){
            log.info("Report not generated: "+e.getMessage());
        }
        return failed;
    }

    @Override
    public void startAnaxTest(Test test){ }

    @Override
    public void endAnaxTest(Test test){
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        String testUniqueID = getUniqueUuid(test,testMethod);
        final TestResult result = createTestResult(test, testMethod);
        getLifecycle().scheduleTestCase(testUniqueID, result);
        getLifecycle().startTestCase(testUniqueID);

        if (videoEnable) {
            try {

                File base = new File(videoBaseDirectory);
                base.mkdirs();
                videoMaker = new VideoMaker(new File(videoBaseDirectory + "/" + testUniqueID + ".mov").toPath(),
                        videoFramesPerSec, videoWaitSeconds);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            log.warn("Video recording feature disabled");
        }
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        String testUniqueID = getUniqueUuid(test,testMethod);

        if (videoEnable) {
           if (videoMaker != null) {
               try {
                   videoMaker.completeVideo();

                   switch (getStepStatus(testMethod)) {
                       case SKIPPED:
                       case PASSED:
                           //NOOP
                           break;
                       case FAILED:
                       case BROKEN:
                           getLifecycle().updateTestCase(testUniqueID, setRecording(testUniqueID));
                           break;

                   }
               } catch (Exception e) {
                   log.error("Failed to complete video recording - recordings enabled? {}",e.getMessage(), e);
               }
           }
        }

        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStep(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setSeverity(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setAnaxIssue(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setFlaky(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setLink(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setLinks(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setIssue(testMethod));
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setIssues(testMethod));
        getLifecycle().updateTestCase(testUniqueID, setStatus(getStepStatus(testMethod)));
        if(testMethod.isPassed()){getLifecycle().updateTestCase(testUniqueID, setPassStdOut(testMethod));}

        getLifecycle().stopTestCase(testUniqueID);
        getLifecycle().writeTestCase(testUniqueID);
    }

    @Override
    public void addFailure(Test test, TestMethod testMethod, Throwable throwable) {
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.FAILED,throwable, testMethod));
        try {
            failed = true;
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addSkipped(Test test, TestMethod testMethod, String skipReason) {
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.SKIPPED,skipReason));
        try {
            failed = true;
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addError(Test test, TestMethod testMethod, Throwable throwable) {
        getLifecycle().updateTestCase(getUniqueUuid(test,testMethod), setStatus(Status.FAILED,throwable,testMethod));
        try {
            failed = true;
            takeScreenshotOnFailure();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Consumer<TestResult> setStatus(final Status status) {

        return result -> {
            result.setStatus(status);
        };

    }

    private Consumer<TestResult> setStatus(final Status status, Throwable throwable, TestMethod method) {
        return result -> {
            result.setStatus(status);
            StatusDetails det = new StatusDetails();

            StringBuilder html = new StringBuilder();
            if (throwable!=null) {
                det.setMessage(throwable.getMessage()); //TODO find the correct message
                result.setStatusDetails(det);
                StringWriter wr = new StringWriter();
                throwable.printStackTrace(new PrintWriter(wr));

                html.append(expandableHtmlSection("Console Logs", List.of(StringUtils.substringAfter(method.getStdOut().toString(), "Caused by:"))));
                html.append(controller instanceof VoidController ? "" : expandableHtmlSection("Browser Logs", controller.getBrowserLogs().stream().map(LogEntry::toString).collect(Collectors.toList())));
                html.append(expandableHtmlSection("Exception Detail", List.of(StringEscapeUtils.escapeHtml4(wr.toString()))));
                result.setDescriptionHtml(html.toString());
            }
        };
    }

    private Consumer<TestResult> setPassStdOut(TestMethod method) {
        return result -> {
            StringBuilder html = new StringBuilder();

            html.append(expandableHtmlSection("Console Logs", List.of(StringUtils.substringAfter(method.getStdOut().toString(), "Caused by:"))));
            html.append(expandableHtmlSection("Special Info", Collections.emptyList()));

            result.setDescriptionHtml(html.toString());
        };
    }

    private Consumer<TestResult> setStatus(final Status status, String reason) {
        return result -> {
            result.setStatus(status);
            result.setDescription(reason);
        };
    }

    private Consumer<TestResult> setSeverity(final TestMethod testMethod) {
        return result -> {
            Severity severityAnnotationLevel = (Severity) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(Severity.class)).findFirst().orElse(null);
            if(severityAnnotationLevel !=null) {
                result.setLabels(
                        List.of(new Label().setName("severity").setValue(severityAnnotationLevel.value().toString()))
                );
            }
        };
    }

    private Consumer<TestResult> setAnaxIssue(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            AnaxIssues issueAnnotationLink = (AnaxIssues) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(AnaxIssues.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.setLinks(Arrays.asList(issueAnnotationLink.issueNames()).stream().map(it->new Link().setType("issue").setName(it.contains("/") ? StringUtils.substringAfterLast(it, "/") : it).setUrl(it)).collect(Collectors.toList()));
            }
        };
    }

    private Consumer<TestResult> setFlaky(final TestMethod testMethod) {

        return result -> {
            io.qameta.allure.Flaky issueAnnotationLink = (io.qameta.allure.Flaky) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Flaky.class)).findFirst().orElse(null);
            if (issueAnnotationLink != null) {
                result.setStatusDetails(result.getStatusDetails() == null ? new StatusDetails().setFlaky(true) : result.getStatusDetails().setFlaky(true));
            }
        };
    }


    private Consumer<TestResult> setLink(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            io.qameta.allure.Link issueAnnotationLink = (io.qameta.allure.Link) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Link.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.setLinks(Collections.singletonList(new Link().setName(issueAnnotationLink.value().contains("/") ? StringUtils.substringAfterLast(issueAnnotationLink.value(),"/") : issueAnnotationLink.value()).setUrl(issueAnnotationLink.url())));
            }
        };
    }

    private Consumer<TestResult> setLinks(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            io.qameta.allure.Links issueAnnotationLink = (io.qameta.allure.Links) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Links.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.setLinks(Arrays.asList(issueAnnotationLink.value()).stream().map(it->new Link().setName(it.value().contains("/") ? StringUtils.substringAfterLast(it.value(),"/") : it.value()).setUrl(it.url())).collect(Collectors.toList()));
            }
        };
    }

    private Consumer<TestResult> setIssue(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            io.qameta.allure.Issue issueAnnotationLink = (io.qameta.allure.Issue) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Issue.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.setLinks(Collections.singletonList(new Link().setName(issueAnnotationLink.value().contains("/") ? StringUtils.substringAfterLast(issueAnnotationLink.value(),"/") : issueAnnotationLink.value()).setUrl(issueAnnotationLink.value())));
            }
        };
    }

    private Consumer<TestResult> setIssues(final TestMethod testMethod) {

        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            io.qameta.allure.Issues issueAnnotationLink = (io.qameta.allure.Issues) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(io.qameta.allure.Issues.class)).findFirst().orElse(null);
            if(issueAnnotationLink !=null) {
                result.setLinks(Arrays.asList(issueAnnotationLink.value()).stream().map(it->new Link().setType("issue").setName(it.value().contains("/") ? StringUtils.substringAfterLast(it.value(),"/") : it.value()).setUrl(it.value())).collect(Collectors.toList()));
            }
        };
    }


    private TestResult createTestResult(Test test, TestMethod testMethod) {
        final String className = test.getTestBean().getClass().getName();
        final String methodName = testMethod.getTestMethod().getName();
        final String name = Objects.nonNull(methodName) ? className+"."+methodName : className;
        final String fullName = Objects.nonNull(methodName) ? String.format("%s.%s", className, methodName) : className;
        String fullName1;
        if(!test.getTestBeanDescription().equals("")){
            if (testMethod.getDataproviderValue()!=null) {
                String s = test.getTestBeanName() + "." + testMethod.getTestMethod().getName() + "_" + testMethod.getDataproviderValue();
                fullName1 = String.format("%s.%s", test.getTestBeanDescription(), Optional.ofNullable(testMethod.getDescription()).orElse(s));
            }
            else if(testMethod.getDatasupplierValue()!=null){
                String s = test.getTestBeanName() + "." + testMethod.getTestMethod().getName() + "_" + testMethod.getDatasupplierValue();
                fullName1 = String.format("%s.%s", test.getTestBeanDescription(), Optional.ofNullable(testMethod.getDescription()).orElse(s));
            }
            else {
                fullName1 = String.format("%s.%s", test.getTestBeanDescription(), Optional.ofNullable(testMethod.getDescription()).orElse(testMethod.getTestMethod().getName()));
            }

        }
        else{
            fullName1 = getUniqueUuid(test,testMethod);
        }

        final TestResult testResult = new TestResult()
                .setUuid(getUniqueUuid(test, testMethod))
                .setHistoryId(getUniqueUuid(test, testMethod))
                .setName(fullName1)
                .setFullName(fullName)
                .setLabels(Arrays.asList(
                        new Label().setName("package").setValue(test.getClass().getPackage().getName()),
                        new Label().setName("testClass").setValue(className),
                        new Label().setName("testMethod").setValue(name),
                        new Label().setName("suite").setValue(suiteName),
                        new Label().setName("host").setValue(getHostName()),
                        new Label().setName("thread").setValue(getThreadName()))
                );
        return testResult;
    }
    private String getUniqueUuid(Test test, TestMethod testMethod) {
        if (testMethod.getDataproviderValue()!=null) {
            String s = test.getTestBeanName() + "." + testMethod.getTestMethod().getName() + "_" + testMethod.getDataproviderValue();
            return s.substring(0, Math.min(s.length(), 100));
        }
        else if(testMethod.getDatasupplierValue()!=null){
            String s = test.getTestBeanName() + "." + testMethod.getTestMethod().getName() + "_" + testMethod.getDatasupplierValue();
            return s.substring(0, Math.min(s.length(), 100));
        }
        else {
            return test.getTestBeanName() + "." + testMethod.getTestMethod().getName();
        }
    }

    private void takeScreenshotOnFailure() throws IOException {
        if (screenshotEnable) {
            AllureAddAttachment();
        } else {
            log.warn("Screenshot feature disabled");
        }
    }

    private void AllureAddAttachment() throws IOException {
        Allure.addAttachment("Screenshot", new ByteArrayInputStream(controller.takeScreenShotAsBytes()));
    }

    private Consumer<TestResult> setStep(final TestMethod testMethod) {
        testMethod.getTestMethod().getDeclaredAnnotations();
        return result -> {
            AnaxTestStep stepDescription = (AnaxTestStep) Arrays.stream(testMethod.getTestMethod().getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().equals(AnaxTestStep.class)).findFirst().orElse(null);
            if (stepDescription != null) {
                if (stepDescription.description().isEmpty()) {
                    result.setSteps(List.of(new StepResult().setName("No available description found.").setStatus(getStepStatus(testMethod))));
                } else {
                    result.setSteps(List.of(new StepResult().setName(stepDescription.description()).setStatus(getStepStatus(testMethod))));
                }

            }
        };
    }

    private Consumer<TestResult> setRecording(String UUID) {
        return result -> {
            Path recording = new File(videoBaseDirectory + "/" + UUID + ".mov").toPath();
            if (recording.toFile().exists()) {
                try (InputStream videoData = Files.newInputStream(recording)) {
                    Allure.addAttachment("Recording." + UUID + ".mov",
                            "video/quicktime", videoData, "mov");
                } catch(Exception e){
                    log.error("Exception when adding video to attachments {}",e.getMessage(),e);
                }
            }
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

            if (videoEnable) {
                FileUtils.deleteQuietly(new File(videoBaseDirectory));
            }
        } catch (IOException e) {
            log.error("Could not generate report: {}", e);
        }
        log.info("Report successfully generated to {}", reportDirectory);
    }

    private String expandableHtmlSection(String sectionTitle, List<String> sectionContent) {
        StringBuilder html = new StringBuilder();
        String toggleScript = "function changeColor(el){var childClass=el.getElementsByTagName('i')[0].classList; " +
                "if (childClass.contains('fa-angle-right')) {childClass.remove('fa-angle-right'); childClass.add('fa-angle-down');} " +
                "else {childClass.add('fa-angle-right'); childClass.remove('fa-angle-down');}}; changeColor(this)";
        html.append("<details open>")
                .append("<summary style=\"cursor:pointer\">")
                .append("<h3 onclick=\"" + toggleScript + "\">")
                .append("<i class=\"fa fa-angle-down\" aria-hidden=\"true\"></i>\n")
                .append(sectionTitle).append("</h3>")
                .append("</summary>");
        sectionContent.forEach(content -> html.append("<pre>").append(content).append("</pre>"));
        html.append("</details>");
        return html.toString();
    }
}
