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

    public void createVideo(Path movieFile, int depth, int framesPerSec) throws Exception {
        if (started) {
            throw new IllegalStateException("Cannot start again, already started");
        }

        System.setProperty("java.awt.headless", Boolean.toString(false));


        MouseCapture mc = MouseCapture.builder()
                .captureDelayMs(1000/framesPerSec)
                .build();
        sc = ScreenCapture.builder()
                .captureDelayMs(1000/framesPerSec)
                .depth(depth)
                .mouseCapture(mc)
                .build();

        started = true;
        mc.captureStart();
        sc.captureStart();

        wr = new QuickTimeWriter(movieFile.toFile());
        wr.addVideoTrack(QuickTimeWriter.VideoFormat.RLE, 1000L, sc.getRect().width, sc.getRect().height);
        //wr.setVideoColorTable(0, (IndexColorModel)sc.getVideoImg().getColorModel());

        pool.scheduleAtFixedRate(() -> {
            final ArrayBlockingQueue<ScreenCapture.Screenshot> screens = sc.getTrackedScreens();

            ArrayList<ScreenCapture.Screenshot> list = new ArrayList<>();
            screens.drainTo(list);
            long t0 = System.currentTimeMillis();
            long prev = 0;
            for (ScreenCapture.Screenshot screenshot : list) {
                try {
                    long duration = (prev == 0) ? (1000 / framesPerSec) : (screenshot.getTime() - prev);
                    wr.writeFrame(0, screenshot.getScreenshot(), duration);
                    prev = screenshot.getTime();
                    log.trace("Frame ts: {} duration {}", screenshot.getTime(),duration);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            log.debug("Writing {} frames to video - time: {}ms", list.size(), System.currentTimeMillis()-t0);


        },100,1000, TimeUnit.MILLISECONDS);

    }

    public void completeVideo() throws Exception {
        if (started) {
            started = false;
            sc.captureEnd();
            log.info("closing....");
            Thread.sleep(1000);
            while (sc.getTrackedScreens().size() > 0) {
                log.info("closing .... frames left {} ...", sc.getTrackedScreens().size());
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
        maker.createVideo( new File("movie.mov").toPath(), 24, 12);
        Thread.sleep(20000);

        maker.completeVideo();


    }

}
