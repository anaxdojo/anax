package org.anax.framework.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.VideoMaker;
import org.anax.framework.controllers.WebController;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReporterSupportsScreenshot;
import org.anax.framework.reporting.ReporterSupportsVideo;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("zapiAnaxTestReporter")
@Slf4j
public class AnaxZapiReporter implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo {
    @Autowired
    protected CycleCreator cycleCreator;
    @Autowired
    protected ExecutionManager executionManager;
    @Autowired
    protected WebController     controller;

    @Value("${zapi.enabled:true}") private Boolean enabled;
    /** do not change the FPS value over 15, due to h/w limitations */
    @Value("${anax.video.fps:10}") Integer videoFramesPerSec;
    /** how many seconds to continue recording, after the "end recording" has been called */
    @Value("${anax.video.waitSecAtEnd:5}") Integer videoWaitSeconds;

    @Value("${zapi.status.pass.code:1}") private String pass;
    @Value("${zapi.status.fail.code:2}") private String fail;
    @Value("${zapi.status.skip.code:4}") private String skip;
    @Value("${zapi.results.directory:zapi-results/}") String resultsZapiDirectory;
    @Value("${zapi.jira.project:NOT_CONFIGURED}") private String project;
    @Value("${zapi.testSteps.status.update:true}") private Boolean testStepStatusUpdateEnabled;
    private VideoMaker          videoMaker;
    private boolean             screenshotEnable;
    private boolean             videoEnable;
    private String              videoBaseDirectory;
    private String              cycleName;
    private String              version;
    private Map<Integer,String> tcComment;
    private Boolean failed   =  false;


    private Set<String>  passedTCs   = new HashSet<>();
    private Set<String>  failedTCs   = new HashSet<>();
    private Set<String>  skippedTCs  = new HashSet<>();
    private Set<String>  errorTCs    = new HashSet<>();
    private List<String> tcSteps     = new ArrayList<>();


    @Autowired
    private AnaxIssueAnnotationResolver anaxIssueAnnotationResolver;


    @Autowired
    public AnaxZapiReporter(AnaxZapiVersionResolver versionResolver){
        version = versionResolver.resolveAppVersion();
    }

    @Override
    public void startOutput(String reportDirectory, String suiteName){

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String out) {

    }

    @Override
    public void startTestSuite(Suite suite) {
        if(enabled) {
            cycleName = suite.getName();
            log.info("************* ZAPI Reporter Start() ***** " + this);
            log.info("********************************************\r\n\r\n");
            log.info("Create Cycle: " + suite.getName() + ", at project: " + project);
            log.info("********************************************\r\n\r\n");

            printSuiteTestCases(suite);
            initialiseCycles(project, version, cycleName);
        }
    }

    @Override
    public boolean endTestSuite(Suite suite){
        if(enabled) {
            log.info("************* ZAPI Reporter onFinish() ***** " + this);
            log.info("********************************************\r\n\r\n");
            log.info("onFinish: Cycle: " + cycleName + ", version: " + version + ", manager: " + executionManager + " - " + this.toString());
            log.info("********************************************\r\n\r\n");

            log.info("Finally: List of Passed TCs: " + passedTCs.toString());
            log.info("Finally: List of Skipped TCs: " + skippedTCs.toString());
            log.info("Finally: List of Failed TCs: " + errorTCs.toString());

            /**
             * Update Test Cases execution Status
             */
            try {
                if (passedTCs.size() != 0) {
                    log.info("Update as PASS the following TCs: " + passedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    executionManager.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(passedTCs), pass);
                }
            } catch (Exception e) {
                log.info("The update of PASSED TCs on jira did not happen due to: " + e.getMessage());
            }

            try {
                if (skippedTCs.size() != 0) {
                    log.info("Update as SKIPPED the following TCs: " + skippedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    executionManager.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(skippedTCs), skip);
                }
            } catch (Exception e2) {
                log.info("The update on SKIPPED jira did not happen due to: " + e2.getMessage());
            }

            try {
                if (failedTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + failedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    executionManager.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(failedTCs), fail);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }

            try {
                if (errorTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + errorTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    executionManager.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(errorTCs), fail);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }
        }

        return failed;
    }


    @Override
    public void startAnaxTest(Test test) {
        if (enabled) {
            if (testStepStatusUpdateEnabled) {
                tcSteps = executionManager.getTestCaseSteps(project, version.trim(), cycleName.trim(), test.getTestBeanName());
                if (CollectionUtils.isEmpty(tcSteps)) {
                    tcComment = new HashMap<>();
                }
            }
        }
    }


    @Override
    public void startTest(Test test, TestMethod testMethod) {
        if(enabled) {

            if (videoEnable) {
                try {
                    File base = new File(videoBaseDirectory);
                    base.mkdirs();
                    videoMaker = new VideoMaker(new File(videoBaseDirectory + "/" + test.getTestBeanName() + "_" + testMethod.getTestMethod().getName() + ".mov").toPath(),
                            videoFramesPerSec, videoWaitSeconds);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                log.warn("Video recording feature disabled");
            }
        }
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        if (enabled) {
            log.info("Identify test status...");
            if (!failedTCs.contains(test.getTestBeanName()) && !skippedTCs.contains(test.getTestBeanName())) {
                passedTCs.add(test.getTestBeanName());
            }

            if(CollectionUtils.isEmpty(tcSteps)){
                if(!testMethod.isPassed() && testMethod.getDescription() != null) {
                    tcComment.put((testMethod.getOrdering() + 1), testMethod.getDescription());
                }
            }

            if (videoEnable) {
                if (videoMaker != null) {
                    try {
                        videoMaker.completeVideo();
                        if(CollectionUtils.isEmpty(tcSteps)) {//attach to tc
                            if (!testMethod.isPassed()) {
                                attachVideoOnTc(test, testMethod);
                            }
                        }

                    } catch (Exception e) {
                        log.info("Failed to complete video recording - recordings enabled? {}", e.getMessage(), e);
                    }
                }
            }

            if (testStepStatusUpdateEnabled) {
                if(!CollectionUtils.isEmpty(tcSteps)) {
                    File screenshot = null;
                    File video = null;

                    if(!testMethod.isPassed()) {
                        screenshot = (screenshotEnable) ? takeScreenshotReturnPath(test, testMethod) : null;
                        video = (videoEnable) ? getVideoPath(test,testMethod) : null;
                    }
                    executionManager.updateTestStepStatusAddAttachments(project, version.trim(), cycleName.trim(), test.getTestBeanName(), getTestStepStatusCode(testMethod), testMethod, screenshot, video);
                }
            }
        }
    }

    @Override
    public void endAnaxTest(Test test) {
        if (enabled) {

            errorTCs.forEach(it -> passedTCs.remove(it));
            failedTCs.forEach(it -> passedTCs.remove(it));
            skippedTCs.forEach(it -> passedTCs.remove(it));


            if (testStepStatusUpdateEnabled) {
                if (!passedTCs.contains(test.getTestBeanName()) && CollectionUtils.isEmpty(tcSteps) && !tcComment.isEmpty()) {//is not pass and has no steps
                    executionManager.updateTestExecutionComment(project, version.trim(), cycleName.trim(), test.getTestBeanName(), "Failed Steps:\n" + tcCommentPrettyPrint(tcComment));
                }
            }
            executionManager.updateTestExecutionBugs(project, version.trim(), cycleName.trim(), test.getTestBeanName(),anaxIssueAnnotationResolver.resolveBugsFromAnnotation(test.getTestIssues()));
        }
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        if (enabled) {

            log.info("Added TC on the failedTCs is: " + test.getTestBeanName());
            failed = true;
            failedTCs.add(test.getTestBeanName());
        }
    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {
        if (enabled) {

            log.info("Added TC on the skippedTCs is: " + test.getTestBeanName());
            failed = true;

            skippedTCs.add(test.getTestBeanName());
            if (CollectionUtils.isEmpty(tcSteps)) {//no-steps add attachment on tc execution
                if (screenshotEnable) {
                    File file = takeScreenshotReturnPath(test, method);
                    executionManager.addExecutionAttachment(project, version, cycleName, test.getTestBeanName(), file);
                }
            }
        }
    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t){
        if (enabled) {

            log.info("Added TC on the errorTCs is: " + test.getTestBeanName());
            failed = true;

            errorTCs.add(test.getTestBeanName());
            if (CollectionUtils.isEmpty(tcSteps)) {//no-steps add attachment on tc execution
                if (screenshotEnable) {
                    File file = takeScreenshotReturnPath(test, method);
                    executionManager.addExecutionAttachment(project, version, cycleName, test.getTestBeanName(), file);
                }
            }
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

    //Create cycle
    private void initialiseCycles(String projectName, String versionName, String cycleName) {
        try{
            cycleCreator.createCycleInVersion(projectName,versionName.trim(), cycleName.trim());
        }catch (Exception e){
            log.info("Cycle on Jira was not created due to: "+e.getMessage());
        }
    }

    //Return path of screenshot
    private File takeScreenshotReturnPath(Test test, TestMethod method){
        String path = resultsZapiDirectory + "/" + test.getTestBeanName() + "_" + method.getTestMethod().getName()+".png";
        if (screenshotEnable) {
            try {
                FileUtils.writeByteArrayToFile(new File(path), controller.takeScreenShotAsBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(path);
    }


    //Returns video path
    private File getVideoPath(Test test, TestMethod method){
        return new File(videoBaseDirectory + "/" + test.getTestBeanName() + "_" + method.getTestMethod().getName() + ".mov");
    }

    //attach video on tc when no steps available
    private void attachVideoOnTc(Test test, TestMethod method) {
        File recording = getVideoPath(test,method);
        if (recording.exists()) {
            try {
                executionManager.addExecutionAttachment(project, version, cycleName, test.getTestBeanName(), recording);
                log.info("Attached video..");
            } catch (Exception e) {
                log.error("Exception when adding video to attachments {}", e.getMessage(), e);
            }
        }
    }

    //Returns status code
    private String getTestStepStatusCode(TestMethod testMethod){
        if (testMethod.isPassed()){
            return pass;
        }else if(testMethod.isSkip()){
            return skip;
        }else{
            return fail;
        }
    }

    private void printSuiteTestCases(Suite suite){
        try {
            List<String> tcs = suite.getTests().stream().map(it -> it.getTestBeanName()).collect(Collectors.toList());
            log.info("Start suite: {} with tests: {}", suite.getName(), tcs);
        }catch (Exception e){e.printStackTrace();}
    }

    //Sort map - split entry per line
    private String tcCommentPrettyPrint(Map map){
        TreeMap<Integer, String> treeMap = new TreeMap<>(map);

        String mapAsString = treeMap.keySet().stream()
                .map(key -> "Step"+String.valueOf(key) + "=" + map.get(key))
                .collect(Collectors.joining("\n", "{", "}"));
        return mapAsString;
    }
}
