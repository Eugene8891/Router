package com.midrive.voice.router_annotation.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a class that provide some services or can be forwarded.
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface Route {
    /**
     * Path of route
     */
    String path();

    /**
     * Used to group pages or services.
     */
    String group() default "";

    /**
     * Name of route.
     */
    String name() default "";

    /**
     * The priority of route.
     */
    int priority() default -1;
}
