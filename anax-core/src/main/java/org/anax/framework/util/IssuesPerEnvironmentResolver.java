package org.anax.framework.util;

import org.anax.framework.annotations.AnaxIssues;
import org.anax.framework.annotations.AnaxIssuesContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Resolves which issues are linked with a test for the specific environment
 * (due to {@link AnaxIssues} is repeatable and also optionally can have an {@link AnaxIssues#environment()} specified).
 * <p>
 * Example: Supposing there is a test with the following annotations:
 * </p>
 * - {@code @AnaxIssues(issueNames = {"BUG-1"})} <br>
 * - {@code @AnaxIssues(issueNames = {"BUG-2"}, environment = "ENV_1")} <br>
 * - {@code @AnaxIssues(issueNames = {"BUG-3"}, environment = "ENV_2")} <br>
 * <p>
 * and supposing that {@code spring.profiles.active=ENV_1} <br>
 * <p>
 * Then the linked issues will be {@code BUG-1, BUG-2}
 */
@Component
public class IssuesPerEnvironmentResolver {

    @Value("${spring.profiles.active:#{null}}")
    String environment;

    /**
     * Returns a list containing the issue names of this method that are linked for this environment
     *
     * @param testMethod
     * @return
     */
    public List<String> getIssueNamesOfTest(Method testMethod) {
        List<String> issueNames = new ArrayList<>();
        List<AnaxIssues> anaxIssuesList = new ArrayList<>();
        Annotation anaxIssuesContainer = Arrays.stream(testMethod.getDeclaredAnnotations()).filter(annotation -> annotation.annotationType().equals(AnaxIssuesContainer.class)).findFirst().orElse(null);
        Annotation anaxIssues = Arrays.stream(testMethod.getDeclaredAnnotations()).filter(annotation -> annotation.annotationType().equals(AnaxIssues.class)).findFirst().orElse(null);
        if (anaxIssuesContainer != null) {
            anaxIssuesList.addAll(Arrays.asList(((AnaxIssuesContainer) anaxIssuesContainer).value()));
        } else if (anaxIssues != null) {
            anaxIssuesList.add(((AnaxIssues) anaxIssues));
        }
        anaxIssuesList.forEach(anaxIssue -> {
            if (!StringUtils.hasLength(anaxIssue.environment()) || anaxIssue.environment().toLowerCase().trim().equals(environment.toLowerCase().trim())) {
                Arrays.stream(anaxIssue.issueNames()).forEach(issueName -> {
                    if (issueName != null && !issueName.isEmpty() && !issueNames.contains(issueName)) {
                        issueNames.add(issueName);
                    }
                });
            }
        });
        return issueNames;
    }
}
