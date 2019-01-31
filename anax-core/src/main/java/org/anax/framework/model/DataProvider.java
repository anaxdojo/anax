package org.anax.framework.model;

import java.util.List;

public interface DataProvider<T> {

    <Τ> List<T> provideTestData();
}
