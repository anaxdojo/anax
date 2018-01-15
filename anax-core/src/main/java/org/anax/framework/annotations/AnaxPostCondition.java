package org.anax.framework.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxPostCondition {

    /**
     * method name as postcondition
     * @return
     */
    String[] methodNames();

    boolean skip() default false;
}
