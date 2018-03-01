package org.anax.framework.capture;


import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.qt.QuickTimeWriter;

import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
public class VideoMaker {

    private ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    private QuickTimeWriter wr = null;
    private ScreenCapture sc;

    private boolean started = false;
    private int finalVideoWait;

    public void createVideo(final Path movieFile,final int framesPerSec, final int videoWaitAfterEndSeconds) throws Exception {
        finalVideoWait = videoWaitAfterEndSeconds*1000;
        if (started) {
            throw new IllegalStateException("Cannot start again, already started");
        }

        System.setProperty("java.awt.headless", Boolean.toString(false));


        MouseCapture mc = MouseCapture.builder()
                .captureDelayMs(1000/framesPerSec)
                .build();
        sc = ScreenCapture.builder()
                .captureDelayMs(1000/framesPerSec)
                .depth(24)
                .mouseCapture(mc)
                .build();

        started = true;
        mc.captureStart();
        sc.captureStart();

        wr = new QuickTimeWriter(movieFile.toFile());
        wr.addVideoTrack(QuickTimeWriter.VideoFormat.RLE, 1000L, sc.getRect().width, sc.getRect().height);
        //wr.setVideoColorTable(0, (IndexColorModel)sc.getVideoImg().getColorModel());

        pool.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final ArrayBlockingQueue<ScreenCapture.Screenshot> screens = sc.getTrackedScreens();

            ArrayList<ScreenCapture.Screenshot> list = new ArrayList<>(100);
            screens.drainTo(list);
            long t0 = System.currentTimeMillis();
            long prev = 0;
            try {
                for (ScreenCapture.Screenshot screenshot : list) {
                    long duration = (prev == 0) ? (1000 / framesPerSec) : (screenshot.getTime() - prev);
                    wr.writeFrame(0, screenshot.getScreenshot(), duration);
                    prev = screenshot.getTime();
                    log.trace("Frame ts: {} duration {}", screenshot.getTime(),duration);
                    if (1000/duration < framesPerSec-1) {
                        log.trace("Requested {} frames/sec, actual {} frames/sec",
                                framesPerSec, 1000/duration);
                    }
                }
            } catch (IOException e) {
                log.error("Exception {} while writing captured video - {} frames lost", e.getMessage(), list.size());
            }
            //log.debug("Writing {} frames to video - time: {}ms", list.size(), System.currentTimeMillis()-t0);


        },5,1, TimeUnit.SECONDS);

    }

    public void completeVideo() throws Exception {
        if (started) {
            // wait for 2 more seconds
            Thread.sleep(finalVideoWait);
            started = false;
            sc.captureEnd();
            log.info("closing .... frames left {}/{} ...", sc.getTrackedScreens().size(),sc.getTrackedScreens().remainingCapacity());
            Thread.sleep(1000);
            while (sc.getTrackedScreens().size() > 0) {
                log.info("waiting .... frames left {}/{} ...", sc.getTrackedScreens().size(),sc.getTrackedScreens().remainingCapacity());
                Thread.sleep(1000);
            }
            pool.shutdownNow();
            while (!pool.isTerminated()) {
                log.info("waiting for writer to finish");
                Thread.sleep(1000);
            }

            if (wr != null) {
                wr.close();
            }
        } else {
            throw new IllegalStateException("Not started, cannot complete!");
        }
    }




    public static void main(String[] args) throws Exception {
        VideoMaker maker = new VideoMaker();
        maker.createVideo( new File("movie.mov").toPath(), 12, 5);
        Thread.sleep(120000);

        maker.completeVideo();


    }

}
