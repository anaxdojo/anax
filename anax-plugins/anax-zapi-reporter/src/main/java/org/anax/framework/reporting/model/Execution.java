package org.anax.framework.reporting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Execution {
    String id;
    String orderId;
    String cycleName;
    String versionName;
    String issueKey;
    String issueId;
    String versionId;
    String projectId;
    String cycleId;

}
