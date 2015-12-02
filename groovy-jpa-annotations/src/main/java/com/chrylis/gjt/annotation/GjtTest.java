package com.chrylis.gjt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass("com.chrylis.gjt.transform.GjtTestAstTransformation")
public @interface GjtTest {

}
