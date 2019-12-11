package org.anax.framework.reporting.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {
    String self;
    String id;
    String description;
    String name;
    Boolean archived;
    Boolean released;
    Integer projectId;
}
