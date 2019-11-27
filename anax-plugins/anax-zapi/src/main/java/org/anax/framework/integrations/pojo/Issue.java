package org.anax.framework.integrations.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Issue {
    String id;
    String key;
}
