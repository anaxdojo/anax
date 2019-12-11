package org.anax.framework.reporting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CycleInfo{
    String id;
    String name;
    String jiraBuildNo;
    String environment;
    String description;
    String startDate;
    String endDate;
    String projectId;
    String versionId;
}