package org.anax.framework.model;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@Builder
public class TestMethod {
    Method testMethod;
    int ordering;
    boolean skip;

}
