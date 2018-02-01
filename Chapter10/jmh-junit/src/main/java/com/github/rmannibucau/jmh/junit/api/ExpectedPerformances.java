package com.github.rmannibucau.jmh.junit.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
public @interface ExpectedPerformances {
    double score();
    double scoreTolerance() default 0.1;
}
