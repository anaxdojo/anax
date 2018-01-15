package org.anax.framework.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxPreCondition {

    /**
     * method name as precondition
     * @return
     */
    String[] methodName();

    boolean skip() default false;

}
