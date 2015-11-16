package com.chrylis.gjt.transform;

import static org.codehaus.groovy.ast.ClassHelper.make;

import java.lang.reflect.Modifier;

import javax.persistence.Version;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.GjtVersion;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GjtVersionAstTransformation extends AbstractGjtAstTransformation<GjtVersion> {

    public static final String VERSION_FIELD_NAME = "version";
    
    @Override
    public Class<GjtVersion> annotationClass() {
        return GjtVersion.class;
    }

    @Override
    public boolean canApplyToClassOnly() {
        return true;
    }

    @Override
    protected void doVisit(AnnotationNode annotationNode, AnnotatedNode annotatedNode) {
        ClassNode entityClass = (ClassNode) annotatedNode;
        
        FieldNode versionField = entityClass.getField(VERSION_FIELD_NAME);
        
        // don't add the version field if it already exists on this class or is visible from a superclass
        if (versionField != null) {
            if(versionField.getOwner().equals(entityClass)) {
                addError("Class annotated with GjtVersion cannot have a version field declared", versionField);
            } else if (!Modifier.isPrivate(versionField.getModifiers())) {
                addError("Class annotated with GjtVersion cannot have a version field declared because the field exists in the parent class: " + versionField.getOwner().getName(), versionField);
            }
            
            return;
        }
        
        PropertyNode versionProperty = entityClass.addProperty(VERSION_FIELD_NAME, ACC_PUBLIC, make(Long.class), new ConstantExpression(null), null, null);
        versionField = versionProperty.getField();
        versionField.setModifiers(ACC_PRIVATE);
        versionField.addAnnotation(new AnnotationNode(make(Version.class)));
    }
}
