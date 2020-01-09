package org.anax.framework.reporting.service;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface AnaxIssueAnnotationResolver {

    /**
     * Resolve the bug from issue / link annotation on each test step
     * call.
     *
     * @param issues
     * @return
     */
    List<String> resolveBugsFromAnnotation(List<String> issues);
}
