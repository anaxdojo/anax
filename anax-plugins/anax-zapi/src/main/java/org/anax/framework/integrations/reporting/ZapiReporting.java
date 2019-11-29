package org.anax.framework.integrations.reporting;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.VideoMaker;
import org.anax.framework.controllers.WebController;
import org.anax.framework.integrations.CycleCreator;
import org.anax.framework.integrations.ExecutionManager;
import org.anax.framework.integrations.pojo.CycleInfo;
import org.anax.framework.integrations.pojo.ExecutionStatus;
import org.anax.framework.integrations.service.AnaxZapiVersionResolver;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class ZapiReporting implements AnaxTestReporter, ReporterSupportsScreenshot, ReporterSupportsVideo {
    /** do not change the FPS value over 15, due to h/w limitations */
    @Value("${anax.allure.video.fps:10}") Integer videoFramesPerSec;
    /** how many seconds to continue recording, after the "end recording" has been called */
    @Value("${anax.allure.video.waitSecAtEnd:5}") Integer videoWaitSeconds;
    @Value("${jira.project:NOT_CONFIGURED}") private String project;
    @Value("${zapi.enabled:true}") private Boolean enabled;

    @Autowired
    protected CycleCreator cycleCreator;

    @Autowired
    protected ExecutionManager updateTests;

    @Autowired
    WebController controller;

    private VideoMaker videoMaker;
    private boolean screenshotEnable;
    private boolean videoEnable;
    private String videoBaseDirectory;
    private String cycleName;
    private String version ; //= "Geno 19.9.hot1";



    private Set<String> passedTCs = new HashSet<String>();
    private Set<String> failedTCs = new HashSet<String>();
    private Set<String> skippedTCs = new HashSet<String>();
    private Set<String> errorTCs = new HashSet<String>();

    @Autowired
    public ZapiReporting(AnaxZapiVersionResolver versionResolver){
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

            initialiseCycles(project, version, cycleName);
        }
    }

    @Override
    public boolean endTestSuite(Suite suite){
        if(enabled) {
            log.info("************* ZAPI Reporter onFinish() ***** " + this);
            log.info("********************************************\r\n\r\n");
            log.info("onFinish: Cycle: " + cycleName + ", version: " + version + ", manager: " + updateTests + " - " + this.toString());
            log.info("********************************************\r\n\r\n");

            log.info("Finally: List of Passed TCs: " + passedTCs.toString());
            log.info("Finally: List of Skipped TCs: " + skippedTCs.toString());
            log.info("Finally: List of Failed TCs: " + errorTCs.toString());

            errorTCs.forEach(it -> passedTCs.remove(it));

            /**
             * Update Test Cases execution Status
             */
            try {
                if (passedTCs.size() != 0) {
                    log.info("Update as PASS the following TCs: " + passedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(passedTCs), ExecutionStatus.PASS);
                }
            } catch (Exception e) {
                log.info("The update of PASSED TCs on jira did not happen due to: " + e.getMessage());
            }

            try {
                if (failedTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + failedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(failedTCs), ExecutionStatus.FAIL);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }

            try {
                if (skippedTCs.size() != 0) {
                    log.info("Update as SKIPPED the following TCs: " + skippedTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(skippedTCs), ExecutionStatus.SKIPPED);
                }
            } catch (Exception e2) {
                log.info("The update on SKIPPED jira did not happen due to: " + e2.getMessage());
            }

            try {
                if (errorTCs.size() != 0) {
                    log.info("Update as FAIL the following TCs: " + errorTCs.toString() + " at version: " + version.trim() + " on cycle: " + cycleName.trim());
                    updateTests.updateTestExecutions(project, version.trim(), cycleName.trim(), new ArrayList<>(errorTCs), ExecutionStatus.FAIL);
                }
            } catch (Exception e1) {
                log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
            }
        }

        return false;
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
//        if (videoEnable) {
//            try {
//                videoMaker = new VideoMaker();
//                File base = new File(videoBaseDirectory);
//                base.mkdirs();
//                videoMaker.createVideo(new File(videoBaseDirectory+"/"+test.getTestBeanName()+testMethod.getTestMethod().getName()+".mov").toPath(),
//                        videoFramesPerSec, videoWaitSeconds);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        } else {
//            log.warn("Video recording feature disabled");
//        }
    }

    @Override
    public void endTest(Test test, TestMethod testMethod) {
        if(enabled) {
            log.info("Identify if test has passed");
            if (!failedTCs.contains(test.getTestBeanName()) && !skippedTCs.contains(test.getTestBeanName())) {
                log.info("Added TC on the passedTCs is: " + test.getTestBeanName());
                passedTCs.add(test.getTestBeanName());
            }
        }
//        if (videoEnable) {
//            if (videoMaker != null) {
//                try {
//                    videoMaker.completeVideo();
//                } catch (Exception e) {
//                    log.info("Failed to complete video recording - recordings enabled? {}",e.getMessage(), e);
//                }
//            }
//        }
    }

    @Override
    public void addFailure(Test test, TestMethod method, Throwable t) {
        log.info("Added TC on the failedTCs is: "+test.getTestBeanName());
        failedTCs.add(test.getTestBeanName());
    }

    @Override
    public void addSkipped(Test test, TestMethod method, String skipReason) {
        log.info("Added TC on the skippedTCs is: "+test.getTestBeanName());
        skippedTCs.add(test.getTestBeanName());
    }

    @Override
    public void addError(Test test, TestMethod method, Throwable t){
        log.info("Added TC on the errorTCs is: "+test.getTestBeanName());
        errorTCs.add(test.getTestBeanName());
        takeScreenshotOnFailure(test,method);
    }

    public final void initialiseCycles(String projectName, String versionName, String cycleName) {
        try{
            cycleCreator.createCycleInVersion(projectName,versionName.trim(), cycleName.trim());
        }catch (Exception e){
            log.info("Cycle on Jira was not created due to: "+e.getMessage());
        }
    }

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MMM/yy");

    /**
     * Set the revision and start date for the cycle creation
     */
    private static CycleInfo getCycleInfo(String environment, String buildNo) {
        LocalDateTime ldtStart = LocalDateTime.now();
        String startTime = dateTimeFormatter.format(ldtStart);
        return CycleInfo.builder().jiraBuildNo(buildNo)
                .environment(environment)
                .startDate(startTime).build();
    }

    private void takeScreenshotOnFailure(Test test,TestMethod method){
        if (screenshotEnable) {
            try {
                FileUtils.writeByteArrayToFile(new File(videoBaseDirectory + "/" + test.getTestBeanName() + "_" + method.getTestMethod().getName()+".png"), controller.takeScreenShotAsBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateTests.addExecutionAttachement(project, version, cycleName, test.getTestBeanName(), new File(videoBaseDirectory + "/" + test.getTestBeanName() + "_" + method.getTestMethod().getName()+".png"));
        }
    }

//    private void setRecording(String UUID) {
//        return result -> {
//            Path recording = new File(videoBaseDirectory + "/" + UUID + ".mov").toPath();
//            if (recording.toFile().exists()) {
//                try (InputStream videoData = Files.newInputStream(recording)) {
//                    updateTests.addExecutionAttachement("Recording." + UUID + ".mov", "video/quicktime", videoData, "mov");
//                } catch(Exception e){
//                    log.error("Exception when adding video to attachments {}",e.getMessage(),e);
//                }
//            }
//        };
//    }

    @Override
    public void screenshotRecording(boolean enable) {
        screenshotEnable = enable;
    }

    @Override
    public void videoRecording(boolean enable, String videoBaseDirectory) {
        this.videoEnable = enable;
        this.videoBaseDirectory = videoBaseDirectory;
    }

}
