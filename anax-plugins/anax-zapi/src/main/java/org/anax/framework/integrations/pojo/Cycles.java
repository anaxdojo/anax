package org.anax.framework.integrations.pojo;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Cycles {
    Map<String,Map<Object,Object>> contents;
    Integer recordCounts;


    public Cycles(Map<String, Map<Object,Object>> map) {
        this.contents = map;
        this.contents.remove("recordsCount");
    }

//    public String getCycleId(String cycleName){
//        for(Map.Entry<String, Object> entry : contents.entrySet()){
//            Map<String,Object> value = (Map<String,Object>) entry.getValue();
//            if(value.get("name").toString().equals(cycleName)){
//                return entry.getKey();
//            }
//        }
//        return null;
//    }

//    public List<String> getCycleNames(){
//       List<String> cycleNames = new ArrayList<String>();
//        for(Map.Entry<String, Object> entry : contents.entrySet()) {
//           Map<String,Object> value = (Map<String,Object>) entry.getValue();
//           cycleNames.add(value.get("name").toString());
//       }
//        return cycleNames;
//    }
//
//    public String getCycleBuild(String cycleName){
//        for(Map.Entry<String, Object> entry : contents.entrySet()){
//            Map<String,Object> value = (Map<String,Object>) entry.getValue();
//            if(value.get("name").toString().equals(cycleName)){
//                return value.get("build").toString();
//            }
//        }
//        return null;
//    }
}
