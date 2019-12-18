package org.anax.framework.capture;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.direct.DirectRobot;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter @Setter
@Slf4j
public class ScreenCapture implements Capture {

    private int fps = 15; //15 fps

    private int bufferSeconds = 360; // 6minutes

    private ThreadLocal<int[]> localScreenPixels;


    private ArrayBlockingQueue<Screenshot> trackedScreens;

    private ScheduledExecutorService pool;

    private MouseCapture mouseCapture;

    private DirectRobot[] robots;

    private int depth = 24;
    private Rectangle rect;


    private int fpsPerThread; //calculated later
    private int threads;

    private Cursor cursor;
    private BufferedImage cursorImg;

    private long time;
    private GraphicsEnvironment env;

    public ScreenCapture(int fps, MouseCapture mouseCapture, int depth) {
        this.fps = fps;
        this.mouseCapture = mouseCapture;
        this.depth = depth;
    }

    public ScreenCapture(int fps, int depth) {
        this.fps = fps;
        this.depth = depth;
    }

    private void init() {
        Window window = new Window(null);
        GraphicsConfiguration cfg = window.getGraphicsConfiguration();
        env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //lets calculate the threads needed: 6 FPS per thread is a good value
        threads = (int) Math.ceil(fps / 6.0);
        fpsPerThread = (int) ((double) fps / (double) threads);
        pool = Executors.newScheduledThreadPool(threads);
        log.info("Capture at {} FPS, {} per thread, {} threads", fps, fpsPerThread, threads);
        robots = new DirectRobot[threads];
        capturing = true; // enable capturing
        try {
            for (int i = 0; i < threads; i++) {
                robots[i] = (new DirectRobot(cfg.getDevice()));
            }
            rect = cfg.getBounds();
            ClassPathResource resource = new ClassPathResource("Cursor.white.png");
            cursorImg = ImageIO.read(resource.getInputStream());
        } catch (IOException ioe) {
            log.error("IO Exception {} ",ioe.getMessage(), ioe);
        } catch (AWTException awt) {
            log.error("AWT Exception {} ",awt.getMessage(), awt);
        } finally {
            if (window != null)
                window.dispose();
        }
        // setup a buffer for us
        if (trackedScreens == null) {
            trackedScreens = new ArrayBlockingQueue<>(fps * bufferSeconds);
            log.info("Allocating buffer for {} seconds [{} frames]", bufferSeconds, trackedScreens.remainingCapacity());
        }

        if (localScreenPixels == null) {
            localScreenPixels = ThreadLocal.withInitial(() -> new int[rect.width*rect.height]);
        }

        switch (depth) {
            case 16: {
                throw new IllegalArgumentException("Cannot process 16 bit images at the moment. Sorry");
//model = new DirectColorModel(16, 0x7C00, 0x3E0, 0x1F);
            }
            case 24: {
                model = new DirectColorModel(24, 0xff00, 0xff00, 0xff, 0xff0000);
                break;
            }
            case 32: {
                throw new IllegalArgumentException("Cannot process 32 bit images at the moment. Sorry");
//model = new DirectColorModel(32, 0xFF0000, 0xFF00, 0xFF, 0xff000000);
            }
        }

    }

    ColorModel model;

    boolean capturing;

    public void captureStart() {
        init();
        log.info("Capturing video at size {}x{} and {} fps ({}ms, {} fps/thread)",
                rect.width, rect.height, fps, (1000.0 / fps), fpsPerThread);

        for (int i = 0; i < threads; i++) {
            final DirectRobot robot = robots[i];
            pool.scheduleAtFixedRate(() -> {
                if (!capturing) return;
                long t0 = System.currentTimeMillis();
                try {
                    BufferedImage screenCapture = grabSnapshotDirect(robot);
                    if (mouseCapture != null)
                        drawMousePointer(t0, screenCapture);
                    trackedScreens.offer(new Screenshot(screenCapture, System.currentTimeMillis()));//add time of capture
                } catch (Exception e) {
                    log.error("Exception {}", e.getMessage());
                }
                long t1 = System.currentTimeMillis();

            }, fractionToNanos(i, threads), fractionToNanos(1, fpsPerThread), TimeUnit.NANOSECONDS);
        }
    }

    final static long toNanos = 1000_000_000L;

    private static long fractionToNanos(long a, long b) {
        return a * toNanos / b;
    }

    private BufferedImage grabSnapshotDirect(DirectRobot robot) {
        robot.getRGBPixels(0,0,rect.width,rect.height, localScreenPixels.get());
        if (depth >= 24) {
            return new BufferedImage(model, Raster.createWritableRaster(
                    model.createCompatibleSampleModel(rect.width, rect.height),
                    new DataBufferInt(localScreenPixels.get(), rect.width * rect.height), null), false, new Hashtable<Object, Object>());
        } else {
            throw new IllegalArgumentException("Cannot process 16 bit images at the moment. Sorry");
        }
    }

    private void drawMousePointer(long now, BufferedImage captured) {
        Graphics2D videoGraphics = env.createGraphics(captured);

        videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_DISABLE);
        videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);

        MouseCapture.MouseMovement mv = mouseCapture.getTrackedMovements().peek();
        while (mv != null) {
            MouseCapture.MouseMovement pc = mouseCapture.getTrackedMovements().remove();
            Point p = pc.getLocation();
            if ((lastLocation != null) && (lastLocation.distance(p) > 2)) { // more than 2px
                videoGraphics.drawImage(cursorImg, p.x, p.y, null);
            }
            lastLocation = p;
            mv = mouseCapture.getTrackedMovements().peek();
        }
    }

    private Point lastLocation = null;

    public void captureEnd() {
        if (mouseCapture != null)
            mouseCapture.captureEnd();
        capturing = false;
        // pool.shutdownNow();
    }


    @AllArgsConstructor
    @Getter
    static
    class Screenshot {
        private BufferedImage screenshot;
        private long time;
    }


}

class Colors
{
    private Colors() {}

    public static IndexColorModel createMacColors()
    {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++)
                {
                    r[index] = ((byte)(255 - 51 * i));
                    g[index] = ((byte)(255 - 51 * j));
                    b[index] = ((byte)(255 - 51 * k));
                    index++;
                }
            }
        }
        index--;

        byte[] ramp = { -18, -35, -69, -86, -120, 119, 85, 68, 34, 17 };
        for (int i = 0; i < 10; i++)
        {
            r[index] = ramp[i];
            g[index] = 0;
            b[index] = 0;
            index++;
        }
        for (int j = 0; j < 10; j++)
        {
            r[index] = 0;
            g[index] = ramp[j];
            b[index] = 0;
            index++;
        }
        for (int k = 0; k < 10; k++)
        {
            r[index] = 0;
            g[index] = 0;
            b[index] = ramp[k];
            index++;
        }
        for (int ijk = 0; ijk < 10; ijk++)
        {
            r[index] = ramp[ijk];
            g[index] = ramp[ijk];
            b[index] = ramp[ijk];
            index++;
        }
        IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);
        return icm;
    }
}

