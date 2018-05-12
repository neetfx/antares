package com.github.prchen.test.antares.starter.annotations;

import com.github.prchen.antares.starter.AntaresManifestAdvice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AntaresManifestAdvice
public @interface Bar {
}
