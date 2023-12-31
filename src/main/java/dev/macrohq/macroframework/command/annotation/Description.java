package dev.macrohq.macroframework.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Description {
    String value() default "";
    String description() default "";
    String[] autoCompletesTo() default "";
}
