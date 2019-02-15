package org.anax.framework.examples.demotestapp.tests;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class TestDataObj {
    String name;
    Integer age;
    Date birthday;

    @Override
    public String toString() {
        return "TestDataObj{" +
                "name='" + name + '\'' +
                '}';
    }
}
