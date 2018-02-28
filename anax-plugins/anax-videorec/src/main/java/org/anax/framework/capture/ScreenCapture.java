package org.anax.framework.capture;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Collections;
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
    private ArrayBlockingQueue<Screenshot> trackedScreens = new ArrayBlockingQueue<Screenshot>(1000);

    @Builder.Default
    private ScheduledExecutorService threadpool = Executors.newScheduledThreadPool(1);

    private MouseCapture mouseCapture;

    private BufferedImage screenCapture;
    private BufferedImage videoImg;

    @Builder.Default
    private int depth = 24;

    private Graphics2D videoGraphics;
    private Rectangle rect;
    private Robot robot;

    private Cursor cursor;
    private BufferedImage cursorImg;

    private long time;


    private void init() {
        Window window = new Window(null);
        GraphicsConfiguration cfg = window.getGraphicsConfiguration();
        try {
            robot = new Robot(cfg.getDevice());
            rect = cfg.getBounds();
            initImage();
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
    }

    private void initImage() throws IOException {
        if (videoGraphics != null) {
            videoGraphics.dispose();
        }
        if (depth == 24) {
            videoImg = new BufferedImage(rect.width, rect.height,
                    1);
        } else if (depth == 16) {
            videoImg = new BufferedImage(rect.width, rect.height,
                    9);
        } else if (depth == 8) {
            videoImg = new BufferedImage(rect.width, rect.height,
                    13, Colors.createMacColors());
        } else {
            throw new IOException("Unsupported color depth " + depth);
        }
        videoGraphics = videoImg.createGraphics();
        videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_DISABLE);
        videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
    }


    public void captureStart() {
        init();
        log.info("Capturing video at size {}x{} and {} fps ({}ms.)",
                rect.width, rect.height, 1000/captureDelayMs, captureDelayMs);
        threadpool.scheduleAtFixedRate(() -> {
            screenCapture = robot.createScreenCapture(new Rectangle(0, 0,
                    rect.width, rect.height));

            long now = System.currentTimeMillis();
            videoGraphics.drawImage(screenCapture, 0, 0, null);
            Point previous = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            while ((mouseCapture.getTrackedMovements().peek() != null) && (mouseCapture.getTrackedMovements().peek().getTime() < now)) {
                MouseCapture.MouseMovement pc = mouseCapture.getTrackedMovements().remove();

                Point p = pc.getLocation();

                videoGraphics.drawImage(cursorImg, p.x, p.y, null);
            }
            //write complete image
            trackedScreens.offer(new Screenshot(videoImg, now));
            log.trace("Image captured");
            //clear our rendering buffer
            try {
                initImage();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }, 0, captureDelayMs, TimeUnit.MILLISECONDS);

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

