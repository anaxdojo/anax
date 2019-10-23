package org.anax.framework.model;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class TestMethod {

    @Builder.Default List<TestMethod> preconditions = new ArrayList<>();
    @Builder.Default List<TestMethod> postconditions = new ArrayList<>();

    Method testMethod;
    int ordering;
    String description;
    boolean skip;
    boolean passed;
    DataProvider dataProvider;
    DataSupplier dataSupplier;
    String providersMethodName = "";

    @Builder.Default StringBuilder stdOut = new StringBuilder();
    @Builder.Default StringBuilder stdErr = new StringBuilder();

}
