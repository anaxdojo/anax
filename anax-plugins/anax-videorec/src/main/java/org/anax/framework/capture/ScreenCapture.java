package org.anax.framework.capture;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.anax.framework.capture.direct.DirectRobot;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.anax.framework.capture.MouseCapture.*;

@Builder
@Getter @Setter
@Slf4j
public class ScreenCapture implements Capture {

    @Builder.Default
    private int captureDelayMs = 1000/10; //10 fps

    @Builder.Default
    private int bufferSeconds = 360; // 6minutes

    ThreadLocal<int[]> localScreenPixels;


    private ArrayBlockingQueue<Screenshot> trackedScreens;

    @Builder.Default
    private ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(16);

    private MouseCapture mouseCapture;

    @Builder.Default
    private int depth = 24;
    private Rectangle rect;
    //private Robot robot;
    private DirectRobot robot;

    private Cursor cursor;
    private BufferedImage cursorImg;

    private long time;

    private GraphicsEnvironment env;

    private void init() {
        Window window = new Window(null);
        GraphicsConfiguration cfg = window.getGraphicsConfiguration();
        env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        try {
            robot = new DirectRobot(cfg.getDevice());
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
            trackedScreens = new ArrayBlockingQueue<>( (1000/captureDelayMs) * bufferSeconds );
            log.info("Allocating buffer for {} seconds [{} frames]", bufferSeconds, trackedScreens.remainingCapacity());
        }

        if (localScreenPixels == null) {
            localScreenPixels = ThreadLocal.withInitial(() -> new int[rect.width*rect.height]);
        }
    }



    public void captureStart() {
        init();
        log.info("Capturing video at size {}x{} and {} fps ({}ms.)",
                rect.width, rect.height, 1000/captureDelayMs, captureDelayMs);
        threadpool.scheduleAtFixedRate(() -> {
            long t0 = System.currentTimeMillis();
            BufferedImage screenCapture = grabSnapshotDirect();
            long now = System.currentTimeMillis();
            drawMousePointer(now, screenCapture);
            trackedScreens.offer(new Screenshot(screenCapture, now));
            long t1 = System.currentTimeMillis();
            if (t1-t0 > captureDelayMs) {
                log.debug("Slow capture speed ({}ms > {}ms)", t1 - t0,captureDelayMs);
            }
        }, 0, captureDelayMs, TimeUnit.MILLISECONDS);

    }

    private BufferedImage grabSnapshotDirect() {
        robot.getRGBPixels(0,0,rect.width,rect.height, localScreenPixels.get());

        ColorModel model = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
        return new BufferedImage(model, Raster.createWritableRaster(
                model.createCompatibleSampleModel(rect.width, rect.height),
                new DataBufferInt(localScreenPixels.get(), rect.width * rect.height), null), false, new Hashtable<Object, Object>());
    }

    private void drawMousePointer(long now, BufferedImage captured) {
        Graphics2D videoGraphics = env.createGraphics(captured);

        videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_DISABLE);
        videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);

        while ((mouseCapture.getTrackedMovements().peek() != null) && (mouseCapture.getTrackedMovements().peek().getTime() < now)) {
            MouseCapture.MouseMovement pc = mouseCapture.getTrackedMovements().remove();
            Point p = pc.getLocation();
            videoGraphics.drawImage(cursorImg, p.x, p.y, null);
        }
    }


    public void captureEnd() {
        mouseCapture.captureEnd();
        threadpool.shutdownNow();
    }


    @AllArgsConstructor
    @Getter
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

