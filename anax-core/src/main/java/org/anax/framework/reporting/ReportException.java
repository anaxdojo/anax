package org.anax.framework.reporting;

import java.io.IOException;

public class ReportException extends Throwable {

    public ReportException(String message) {
        super(message);
    }
    public ReportException(String message, IOException exc) {
        super(message,exc);
    }
}
