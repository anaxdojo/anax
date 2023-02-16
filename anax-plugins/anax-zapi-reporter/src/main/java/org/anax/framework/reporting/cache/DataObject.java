package org.anax.framework.reporting.cache;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataObject {

    private final String data;

    public DataObject(String data) {
        this.data = data;
    }

    public static DataObject get(String data) {
        return new DataObject(data);
    }
}
