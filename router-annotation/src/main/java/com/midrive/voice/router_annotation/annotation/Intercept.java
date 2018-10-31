package com.midrive.voice.router_annotation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a interceptor to interception the route.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Intercept {
    /**
     * The priority of interceptor, Router will excute them follow the priority.
     */
    int priority();

    /**
     * The name of interceptor
     */
    String name() default "";
}
