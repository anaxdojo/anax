package org.anax.framework.annotations;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxTest {

    String[] value() default { "Anax Default Suite" };

    int priority() default 0;

    /**
     * By default test classes run single threaded (1 thread).
     * Configure this parameter to allow parallel execution of tests
     * @return
     */
    int parallelClassThreads() default 1;


}
