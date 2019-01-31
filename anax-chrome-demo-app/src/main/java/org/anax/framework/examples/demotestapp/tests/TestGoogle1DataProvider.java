package org.anax.framework.examples.demotestapp.tests;
import org.anax.framework.model.DataProvider;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class TestGoogle1DataProvider<TestDataObj> implements DataProvider {
    @Override
    public List<TestDataObj> provideTestData() {
        return (List<TestDataObj>) Arrays.asList(org.anax.framework.examples.demotestapp.tests.TestDataObj.builder()
                .name("George")
                .age(46)
                .birthday(new Date()).build(),
                org.anax.framework.examples.demotestapp.tests.TestDataObj.builder()
                        .name("Thanos")
                        .age(42)
                        .birthday(new Date()).build()
                );
    }
}
