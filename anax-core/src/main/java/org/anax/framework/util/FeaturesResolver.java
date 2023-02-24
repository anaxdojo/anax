package org.anax.framework.util;

import org.anax.framework.model.Test;
import org.anax.framework.model.TestMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FeaturesResolver {

    @Value("${anax.exec.features:CORE}")
    String executeFeatures;

    private static final String SEPARATOR = ",";

    /**
     * Parses the {@code featureStr} value, removes any whitespace, turns to uppercase and returns the value as list
     *
     * @param featureStr - Comma separated values
     * @return
     */
    private List<String> getFeaturesFromString(String featureStr) {
        return Arrays.asList(featureStr.toUpperCase().replaceAll("\\s+", "").split(SEPARATOR));
    }

    /**
     * Returns the {@code featureArr} as List, after, removing any whitespace and turning to uppercase all its elements
     *
     * @param featureArr
     * @return
     */
    private List<String> getFeaturesFromArray(String[] featureArr) {
        List<String> features = new ArrayList<>();
        Arrays.asList(featureArr).forEach(feature -> features.add(feature.replaceAll("\\s+", "").toUpperCase()));
        return features;
    }

    /**
     * Returns a list with the features to run
     *
     * @return
     */
    private List<String> getFeaturesToRun() {
        return getFeaturesFromString(executeFeatures);
    }

    /**
     * Checks if the {@testMethod} has to run, according to the features. A TestMethod will run only if a TestMethod feature
     * exists in the Test class features and if this feature also exists in the {@code anax.exec.features}
     *
     * @param test
     * @param testMethod
     * @return
     */
    public boolean evaluateTestMethodFeatures(Test test, TestMethod testMethod) {
        List<String> featuresToRun = getFeaturesToRun();
        List<String> testFeatures = getFeaturesFromArray(test.getFeatures());
        List<String> testMethodFeatures = getFeaturesFromArray(testMethod.getFeatures());
        return testMethodFeatures.stream().anyMatch(testMethodFeature -> testFeatures.contains(testMethodFeature) && featuresToRun.contains(testMethodFeature));
    }

    /**
     * Checks if the Test class has to run, according to the features. A Test will only run if the Test features
     *
     * @param test
     * @return
     */
    public boolean evaluateTestFeatures(Test test) {
        return containsAny(getFeaturesFromArray(test.getFeatures()), getFeaturesToRun());
    }

    /**
     * Checks whether any element of the {@code list1} exists in the {@code list2}
     *
     * @param list1
     * @param list2
     * @param <T>
     * @return
     */
    private <T> boolean containsAny(List<T> list1, List<T> list2) {
        for (T element : list1) {
            if (list2.contains(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility method to check if a feature is enabled
     *
     * @param feature
     * @return
     */
    public boolean isFeatureEnabled(String feature) {
        return getFeaturesToRun().contains(feature.toUpperCase().replaceAll("\\s+", ""));
    }
}
