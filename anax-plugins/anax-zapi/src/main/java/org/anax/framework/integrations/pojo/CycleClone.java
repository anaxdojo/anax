package org.anax.framework.integrations.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class CycleClone {
    String clonedCycleId;
    String name;
    String jiraBuildNo;
    String environment;
    String description;
    String startDate;
    String endDate;
    String projectId;
    String versionId;
    String sprintId;
}