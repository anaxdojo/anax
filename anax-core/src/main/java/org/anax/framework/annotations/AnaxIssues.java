package org.anax.framework.annotations;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxIssues {

    /**
     * issues name
     * @return
     */
    String[] issueNames();

}
