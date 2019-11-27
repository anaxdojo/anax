package org.anax.framework.integrations.pojo;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@Builder
public class ProjectList {
    List<LabelValue> options;
}
