package org.anax.framework.util;

import org.anax.framework.annotations.AnaxIssues;
import org.anax.framework.annotations.AnaxIssuesContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class IssuesPerEnvironmentResolver {

    @Value("${spring.profiles.active:NOT_CONFIGURED}")
    String environment;

    /**
     * Returns the issue names of this method, for this environment
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
            if (anaxIssue.environment() == null || anaxIssue.environment().isEmpty() || anaxIssue.environment().equals(environment)) {
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
