package org.anax.framework.integrations.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TestStepExecution {
    Integer id;
    String status;
    Integer executionId;
    Integer stepId;
    Integer testStepId;
    Integer orderId;
    String step;
    String result;
}