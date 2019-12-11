package org.anax.framework.reporting;

public interface ReporterSupportsVideo {


    /**
     * Uses Reporter capability to record Video - the video stream will
     * include the whole Test session (class level, not method level)
     *
     * @param enable
     */
    void videoRecording(boolean enable, String videoBaseDirectory);


}
