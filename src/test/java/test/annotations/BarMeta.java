package test.annotations;

import com.github.prchen.antares.starter.AntaresManifestAdvice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@AntaresManifestAdvice
public @interface BarMeta {
}
