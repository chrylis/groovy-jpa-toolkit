package com.chrylis.gjt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * Adds a JPA {@code @Version} property to the annotated type.
 * 
 * @author Christopher Smith
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass("com.chrylis.gjt.transform.GjtIdAstTransformation")
public @interface GjtId {
    
    Class<?> type() default Long.class;
    
    String name() default "id";
}
