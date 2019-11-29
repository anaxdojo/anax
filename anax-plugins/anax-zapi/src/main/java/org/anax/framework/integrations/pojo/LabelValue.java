package org.anax.framework.integrations.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LabelValue {
    String value;
    String label;
}
