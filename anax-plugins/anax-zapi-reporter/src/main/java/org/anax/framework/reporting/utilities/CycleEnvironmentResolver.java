package org.anax.framework.reporting.utilities;

import org.anax.framework.reporting.model.CycleInfo;
import org.springframework.stereotype.Component;

@Component
public class CycleEnvironmentResolver {

    public boolean isCycleEnvironmentSameWithRunEnvironment(String versionName, CycleInfo cycleInfo, String runEnvironment) {
        if ("Unscheduled".equals(versionName)) {
            return cycleInfo.getEnvironment() == null || cycleInfo.getEnvironment().isEmpty();
        } else {
            return runEnvironment.equals(cycleInfo.getEnvironment());
        }
    }
}
