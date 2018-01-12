package org.anax.framework.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxBeforeTestStep {

    /**
     * method name as precondition or postcondition
     * @return
     */
    String value() default "";

    boolean skip() default false;

}
