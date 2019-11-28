package org.anax.framework.integrations.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CycleClone {
    String clonedCycleId;
    String name;
    String build;
    String environment;
    String description;
    String startDate;
    String endDate;
    String projectId;
    String versionId;
    String sprintId;


    public CycleClone(String name) {
        this.name = name;
    }
}