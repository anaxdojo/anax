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
                        .name("validationTests.testStep_1_{updated_at:2019-02-01T13:10:05Z,kind:emarsys,name:Emarsys Experiment Connection76791549026604807,created_at:2019-02-01T13:10:05Z,id:402,connect_api_key:8ISORuY8Dhku-s1fwNd5Fri30XdD6MH7YRcNoizTmUuX4opCB4yX1uxbn_of5JBaLgKWeqMkdH_R99aFY2EXq6tSe6W5Yw,emarsys_account_id:213241259,emarsys_host_url:https:api-proxy.s.emarsys.com,user:{account_id:31,application:sensei,user_id:52}}-result.json")
                        .age(42)
                        .birthday(new Date()).build()
                );
    }
}
