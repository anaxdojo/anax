package org.anax.framework.integrations.pojo;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Cycles {
    Map<String,Object> contents;
    Integer recordCounts;


    public Cycles(Map<String, Object> map) {
        this.contents = map;
        this.recordCounts = (Integer) map.get("recordsCount");
        this.contents.remove("recordsCount");
    }

    public String getCycleId(String cycleName) throws NoSuchFieldException {
        for(Map.Entry<String, Object> entry : contents.entrySet()){
            Map<String,Object> value = (Map<String,Object>) entry.getValue();
            if(value.get("name").toString().equals(cycleName)){
                return entry.getKey();
            }
        }
        return null;
    }

    public List<String> getCycleNames(){
       List<String> cycleNames = new ArrayList<String>();
        for(Map.Entry<String, Object> entry : contents.entrySet()) {
           Map<String,Object> value = (Map<String,Object>) entry.getValue();
           cycleNames.add(value.get("name").toString());
       }
        return cycleNames;
    }

    public String getCycleBuild(String cycleName){
        for(Map.Entry<String, Object> entry : contents.entrySet()){
            Map<String,Object> value = (Map<String,Object>) entry.getValue();
            if(value.get("name").toString().equals(cycleName)){
                return value.get("build").toString();
            }
        }
        return null;
    }
}
