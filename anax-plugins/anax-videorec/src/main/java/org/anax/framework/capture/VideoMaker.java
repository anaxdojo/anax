package org.anax.framework.capture;

import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.qt.QuickTimeWriter;
import org.springframework.util.Assert;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class VideoMaker {

    private ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

    private QuickTimeWriter wr = null;
    private ScreenCapture sc;


    private int finalVideoWait;

    public VideoMaker(final Path movieFile, final int framesPerSec, final int videoWaitAfterEndSeconds) throws Exception {
        this(movieFile, framesPerSec, videoWaitAfterEndSeconds, false, 24);
    }

    public VideoMaker(final Path movieFile, final int framesPerSec, final int videoWaitAfterEndSeconds, final boolean captureMouse, final int depth) throws Exception {
        Assert.state(videoWaitAfterEndSeconds > 0 && videoWaitAfterEndSeconds < 20, "Cannot capture more than 20 seconds after end");
        Assert.state(framesPerSec > 6 && framesPerSec <= 30, "Cannot capture less than 7 and more than 30 FPS");
        Assert.state(depth == 16 || depth == 24 || depth == 32, "Cannot capture depth other than 16, 24, 32 bit");
        finalVideoWait = videoWaitAfterEndSeconds*1000;


        System.setProperty("java.awt.headless", Boolean.toString(false));
        if (captureMouse) {
            MouseCapture mc = MouseCapture.builder()
                    .captureDelayMs(1000 / framesPerSec)
                    .build();
            sc = new ScreenCapture(framesPerSec, mc, depth); //rle is 24bit anyway
            mc.captureStart();
        } else {
            sc = new ScreenCapture(framesPerSec, depth);

        }

        sc.captureStart();

        wr = new QuickTimeWriter(movieFile.toFile());
        wr.addVideoTrack("rle ", "Animation", 1000L, sc.getRect().width, sc.getRect().height, depth, 30);

        AtomicLong totalFrames = new AtomicLong(0);
        pool.scheduleWithFixedDelay(() -> {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final ArrayBlockingQueue<ScreenCapture.Screenshot> screens = sc.getTrackedScreens();

            ArrayList<ScreenCapture.Screenshot> list = new ArrayList<>(100);
            screens.drainTo(list);
            list.sort(Comparator.comparingLong(ScreenCapture.Screenshot::getTime)); //sort in case frames arrived later
            long t0 = System.currentTimeMillis();
            try {
                log.info("WR: processing {} frames...", list.size());
                for (ScreenCapture.Screenshot screenshot : list) {
                    double duration = (1000.0 / framesPerSec);
                    wr.writeFrame(0, screenshot.getScreenshot(), (long) duration);
                }
                final long l = totalFrames.addAndGet(list.size());
                log.info("WR: total frames {}, seconds {}", l, (l / framesPerSec));
            } catch (Exception e) {
                log.error("Exception {} while writing captured video - {} frames lost", e.getMessage(), list.size());
            }
        }, 20, 10, TimeUnit.SECONDS);

    }

    public void completeVideo() throws Exception {
        // wait for 2 more seconds
        Thread.sleep(finalVideoWait);
        sc.captureEnd();
        log.info("closing .... frames left {} (max {}) ...", sc.getTrackedScreens().size(), sc.getTrackedScreens().remainingCapacity());

        while (sc.getTrackedScreens().size() > 0) { //waiting for writer to finish
            log.info("waiting .... frames left {} (max {}) ...", sc.getTrackedScreens().size(), sc.getTrackedScreens().remainingCapacity());
            Thread.sleep(5000);
        }
        pool.shutdown();
        while (!pool.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
            log.info("waiting for writer to finish");
        }

        if (wr != null) {
            wr.close();
            log.info("Video complete.");
        }

    }

//    public static void main(String[] args) throws Exception {
//        VideoMaker maker = new VideoMaker( new java.io.File("movie.mov").toPath(), 30, 5);
//        Thread.sleep(60000);
//        maker.completeVideo();
//        System.exit(0);
//    }

}
