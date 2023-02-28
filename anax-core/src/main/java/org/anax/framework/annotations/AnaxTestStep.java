package org.anax.framework.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnaxTestStep {
    /**
     * ordering default 0 implies random order
     */
    int ordering() default 0;

    String description() default "";

    boolean skip() default false;

    String dataprovider() default "";

    String datasupplier() default "";

    /**
     * Link the Test Step with a specific feature. To run it, include the feature value in the comma separated {@code anax.exec.features} property
     *
     * @return
     */
    String[] features() default "CORE";
}
