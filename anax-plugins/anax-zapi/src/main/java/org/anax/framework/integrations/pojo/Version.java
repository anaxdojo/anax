package org.anax.framework.integrations.pojo;

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
