package org.anax.framework.annotations;


import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxTestStep {
    /**
     * ordering default 0 implies random order
     */
    int ordering() default 0;

    boolean skip() default false;

}
