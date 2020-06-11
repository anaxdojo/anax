package org.anax.framework.reporting;

import org.anax.framework.reporting.service.AnaxIssueAnnotationResolver;

import java.util.List;
import java.util.stream.Collectors;

public class IssueToBugAnnotationResolver implements AnaxIssueAnnotationResolver {

    @Override
    public List<String> resolveBugsFromAnnotation(List<String> issues) {
        if(issues.stream().allMatch(it->it.contains("/"))){
            return issues.stream().map(it->it.substring(it.lastIndexOf("/")+1)).collect(Collectors.toList());
        }else {
            return issues;
        }
    }
}
