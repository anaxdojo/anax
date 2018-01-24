package org.anax.framework.configuration;

import org.anax.framework.model.Suite;
import org.anax.framework.reporting.ReportException;

import java.io.OutputStream;

public interface AnaxSuiteRunner {

    void createExecutionPlan(boolean executePlan);

}
