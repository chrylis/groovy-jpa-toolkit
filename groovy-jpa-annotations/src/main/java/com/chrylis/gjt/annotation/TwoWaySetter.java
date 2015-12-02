package com.chrylis.gjt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
@GroovyASTTransformationClass("com.chrylis.gjt.transform.TwoWaySetterAstTransformation")
public @interface TwoWaySetter {
    String mappedBy() default "";
}
