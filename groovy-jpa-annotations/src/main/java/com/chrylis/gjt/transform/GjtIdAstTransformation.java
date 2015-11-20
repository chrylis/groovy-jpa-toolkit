package com.chrylis.gjt.transform;

import javax.persistence.Id;

import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.GjtId;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GjtIdAstTransformation extends AbstractGjtPropertyAddingAstTransformation<GjtId> {

    @Override
    public Class<GjtId> annotationClass() {
        return GjtId.class;
    }

    @Override
    protected Class<?> identifyingAnnotation() {
        return Id.class;
    }
}
