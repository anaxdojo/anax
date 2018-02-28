package org.anax.framework.reporting;

public interface ReporterSupportsScreenshot {

    /**
     * Uses Reporter capability to record Screenshot on errors/failures
     *
     * @param enable
     */
    void screenshotRecording(boolean enable);

}
