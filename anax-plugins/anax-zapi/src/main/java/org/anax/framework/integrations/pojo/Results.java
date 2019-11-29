package org.anax.framework.integrations.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by gkogketsof on 3/19/14.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class Results {
    List<String> executions;
    String status;
}
