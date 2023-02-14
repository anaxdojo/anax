package org.anax.framework.reporting.utilities;

import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.stream.IntStream;

@UtilityClass
public class JsonUtilities {
    /**
     * Find the correct object in a jsonArray based on the value of an attribute,Create a list of label and then get the index of the JsonObject with this label
     *
     * @param jsonArray
     * @param attribute
     * @param labelValue
     * @return
     * @throws JSONException
     */
    public static JSONObject filterDataByAttributeValue(JSONArray jsonArray, String attribute, String labelValue) throws JSONException {
        int index;
        ArrayList<String> values = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            values.add(jsonArray.getJSONObject(i).getString(attribute).toLowerCase());
        }
        index = IntStream.range(0, values.size()).filter(i -> values.get(i).contains(labelValue.toLowerCase())).findFirst().getAsInt();
        return jsonArray.getJSONObject(index);
    }

    /**
     * Find the correct object in a jsonArray based on the value of an attribute,Create a list of label and then get the index of the JsonObject with this label
     *
     * @param jsonArray
     * @param attribute
     * @param labelValue
     * @return
     * @throws JSONException
     */
    public static JSONObject filterDataByIntegerAttributeValue(JSONArray jsonArray, String attribute, int labelValue) throws JSONException {
        int index;
        ArrayList<Integer> values = new ArrayList<>();

        IntStream.range(0, jsonArray.length()).forEach(i -> {
            try {
                values.add(jsonArray.getJSONObject(i).getInt(attribute));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        index = IntStream.range(0, values.size()).filter(i -> values.get(i).equals(labelValue)).findFirst().getAsInt();
        return new JSONObject(jsonArray.getJSONObject(index).toString());
    }
}
