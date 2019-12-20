package org.anax.framework.reporting;

public class ReportException extends Throwable {

    public ReportException(String message) {
        super(message);
    }
    public ReportException(String message, Throwable exc) {
        super(message,exc);
    }
}
