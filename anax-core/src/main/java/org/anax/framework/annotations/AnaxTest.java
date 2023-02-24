package org.anax.framework.annotations;


import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface AnaxTest {

    String[] value() default { "Anax Default Suite" };

    String description() default "";

    int priority() default 0;

    String feature() default "ALL";

}
