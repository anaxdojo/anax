package org.anax.framework.annotations;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AnaxIssuesContainer.class)
@Documented
public @interface AnaxIssues {

    /**
     * issues name
     *
     * @return
     */
    String[] issueNames();

    /**
     * Which environment these issues are
     *
     * @return
     */
    String environment() default "";

}
