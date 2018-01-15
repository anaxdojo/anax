package org.anax.framework.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class Suite {
    private String name;
    @Builder.Default private List<Test> tests = new ArrayList<>();

}
