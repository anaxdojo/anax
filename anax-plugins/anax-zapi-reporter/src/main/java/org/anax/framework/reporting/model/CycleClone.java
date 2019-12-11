package org.anax.framework.reporting.model;

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


    public CycleClone(String name,String startDate) {
        this.name = name;
        this.startDate = startDate;
    }
}