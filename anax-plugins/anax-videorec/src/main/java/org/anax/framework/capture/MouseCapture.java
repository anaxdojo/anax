package org.anax.framework.capture;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;


/**
 * configure and capture mouse movement in a screen
 */
@Data
@Builder
@Slf4j
public class MouseCapture implements Capture {


    @Builder.Default
    private int captureDelayMs = 1000/10; //10 fps

    @Builder.Default
    private Queue<MouseMovement> trackedMovements = new ArrayBlockingQueue<MouseMovement>(1000);

    @Builder.Default
    private ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(1);


    public void captureStart() {
        log.info("Capturing movement at {}ms.",captureDelayMs);
        threadpool.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            PointerInfo info = MouseInfo.getPointerInfo();
            trackedMovements.offer(new MouseMovement(info.getLocation(), now));
            log.trace("Movement captured at point {}",info.getLocation());
        }, 0, captureDelayMs, TimeUnit.MILLISECONDS);
    }


    public void captureEnd() {
        threadpool.shutdownNow();
    }


    @AllArgsConstructor
    @Getter
    class MouseMovement {
        private Point location;
        private long time;
    }

}
