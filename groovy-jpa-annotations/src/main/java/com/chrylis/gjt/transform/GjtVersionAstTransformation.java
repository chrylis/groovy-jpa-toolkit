package com.chrylis.gjt.transform;

import javax.persistence.Version;

import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.GjtVersion;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GjtVersionAstTransformation extends AbstractGjtPropertyAddingAstTransformation<GjtVersion> {

    @Override
    public Class<GjtVersion> annotationClass() {
        return GjtVersion.class;
    }

    @Override
    public boolean canApplyToClassOnly() {
        return true;
    }
    
    @Override
    protected Class<?> identifyingAnnotation() {
        return Version.class;
    }
}
