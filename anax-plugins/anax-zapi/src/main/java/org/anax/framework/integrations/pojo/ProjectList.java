package org.anax.framework.integrations.pojo;

import lombok.*;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Setter
public class ProjectList {
    List<LabelValue> options;
}
