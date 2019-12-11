package org.anax.framework.reporting.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
@Data
@Builder
public class ExecutionList {
    List<Execution> executions;
}
