package org.anax.framework.integrations.reporting;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.VideoMaker;
import org.anax.framework.controllers.WebController;
import org.anax.framework.integrations.CycleCreator;
import org.anax.framework.integrations.ExecutionManager;
import org.anax.framework.integrations.pojo.CycleInfo;
import org.anax.framework.integrations.pojo.ExecutionStatus;
import org.anax.framework.model.Suite;
import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.anax.framework.reporting.AnaxTestReporter;
import org.anax.framework.reporting.ReportException;
import org.anax.framework.reporting.ReporterSupportsScreenshot;
import org.anax.framework.reporting.ReporterSupportsVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Autowired
    protected CycleCreator cycleCreator;

    @Autowired
    protected ExecutionManager updateTests;

    @Autowired
    WebController controller;

    private static String testClassPrefix;
    private static String jiraProjectPrefix;
    private static String cycleName;
    private static String version;
    private VideoMaker videoMaker;
    private boolean screenshotEnable;
    private boolean videoEnable;
    private String videoBaseDirectory;



    private Set<String> passedTCs = new HashSet<String>();
    private Set<String> failedTCs = new HashSet<String>();
    private Set<String> skippedTCs = new HashSet<String>();
    private Set<String> errorTCs = new HashSet<String>();



    @Override
    public void startOutput(String reportDirectory, String suiteName) throws FileNotFoundException {

    }

    @Override
    public void setSystemOutput(String out) {

    }

    @Override
    public void setSystemError(String out) {

    }

    @Override
    public void startTestSuite(Suite suite) throws ReportException {

    }

    @Override
    public boolean endTestSuite(Suite suite) throws ReportException {
        log.info("************* ZAPI Reporter onFinish() ***** "+this);

        //Init it the first time and then set it as static
//        if (updateTests == null) {
//            updateTests = context.getBean(ExecutionManager.class);
//        }

        log.info("********************************************\r\n\r\n");
        log.info("onFinish: Cycle: "+cycleName+", version: "+version+", manager: "+updateTests+" - "+this.toString());
        log.info("********************************************\r\n\r\n");

        log.info(passedTCs.toString());
        log.info(failedTCs.toString());
        log.info(skippedTCs.toString());
        log.info(errorTCs.toString());

        errorTCs.forEach(it-> passedTCs.remove(it));

        /**
         * Update Test Cases execution Status
         */
        try {
            if (passedTCs.size() != 0) {
                log.info("Update as PASS the following TCs: " + passedTCs.toString()+" at version: "+version.trim()+" on cycle: "+cycleName.trim());
                updateTests.updateTestExecutions(jiraProjectPrefix, version.trim(), cycleName.trim(), new ArrayList<String>(passedTCs), ExecutionStatus.PASS);
            }
        }catch (Exception e) {
            log.info("The update of PASSED TCs on jira did not happen due to: " + e.getMessage());
        }

        try{
            if (failedTCs.size() != 0) {
                log.info("Update as FAIL the following TCs: " + failedTCs.toString()+" at version: "+version.trim()+" on cycle: "+cycleName.trim());
                updateTests.updateTestExecutions(jiraProjectPrefix, version.trim(), cycleName.trim(), new ArrayList<String>(failedTCs), ExecutionStatus.FAIL); //Arrays.asList("RallyTC503","MCS-6350")
            }
        }catch (Exception e1) {
            log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
        }

        try{
            if (skippedTCs.size() != 0) {
                log.info("Update as BLOCK the following TCs: " + skippedTCs.toString()+" at version: "+version.trim()+" on cycle: "+cycleName.trim());
                updateTests.updateTestExecutions(jiraProjectPrefix, version.trim(), cycleName.trim(), new ArrayList<String>(skippedTCs), ExecutionStatus.BLOCKED);
            }
        } catch (Exception e2) {
            log.info("The update on SKIPPED jira did not happen due to: " + e2.getMessage());
        }

        try{
            if (errorTCs.size() != 0) {
                log.info("Update as FAIL the following TCs: " + errorTCs.toString()+" at version: "+version.trim()+" on cycle: "+cycleName.trim());
                updateTests.updateTestExecutions(jiraProjectPrefix, version.trim(), cycleName.trim(), new ArrayList<String>(errorTCs), ExecutionStatus.FAIL);
            }
        }catch (Exception e1) {
            log.info("The update of FAILED TCs on jira did not happen due to: " + e1.getMessage());
        }

        return false;
    }

    @Override
    public void startTest(Test test, TestMethod testMethod) {
        if (videoEnable) {
            try {
                videoMaker = new VideoMaker();
                File base = new File(videoBaseDirectory);
                base.mkdirs();
                videoMaker.createVideo(new File(videoBaseDirectory+"/"+test.getTestBeanName()+testMethod.getTestMethod().getName()+".mov").toPath(),
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
        log.info("Identify if test has passed");
        if(!failedTCs.contains(test.getTestBeanName()) && !skippedTCs.contains(test.getTestBeanName())){
            log.info("Added TC on the passedTCs is: "+test.getTestBeanName());
            passedTCs.add(test.getTestBeanName());
        }
        if (videoEnable) {
            if (videoMaker != null) {
                try {
                    videoMaker.completeVideo();
                } catch (Exception e) {
                    ("Failed to complete video recording - recordings enabled? {}",e.getMessage(), e);
                }
            }
        }
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
    public void addError(Test test, TestMethod method, Throwable t) throws IOException {
        log.info("Added TC on the errorTCs is: "+test.getTestBeanName());
        errorTCs.add(test.getTestBeanName());
        takeScreenshotOnFailure();
    }

    public final void initialiseCycles(String environment,String buildNo, String versionNumber, String suiteName, String testClassPref, String jiraProjectPref) {
        testClassPrefix = testClassPref;
        jiraProjectPrefix = jiraProjectPref;
        try{
            CycleInfo cycleInfo = getCycleInfo(environment, buildNo);
            cycleName = cycleCreator.createCycleInVersion(jiraProjectPrefix,versionNumber.trim(), suiteName.trim(),cycleInfo);
            version = versionNumber.trim();

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

    private void takeScreenshotOnFailure() throws IOException {
        if (screenshotEnable) {
            Allure.addAttachment("Screenshot", new ByteArrayInputStream(controller.takeScreenShotAsBytes()));
        } else {
            log.warn("Screenshot feature disabled");
        }
    }

    private void setRecording(String UUID) {
        return result -> {
            Path recording = new File(videoBaseDirectory + "/" + UUID + ".mov").toPath();
            if (recording.toFile().exists()) {
                try (InputStream videoData = Files.newInputStream(recording)) {
                    Allure.addAttachment("Recording." + UUID + ".mov", "video/quicktime", videoData, "mov");
                } catch(Exception e){
                    log.error("Exception when adding video to attachments {}",e.getMessage(),e);
                }
            }
        };
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

}
