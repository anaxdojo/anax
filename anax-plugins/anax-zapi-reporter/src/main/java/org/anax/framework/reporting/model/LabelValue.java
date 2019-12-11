package org.anax.framework.reporting.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabelValue {
    String value;
    String label;
}
