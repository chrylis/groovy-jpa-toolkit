package com.chrylis.gjt.transform;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.make;

import java.lang.reflect.Modifier;
import java.util.List;

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

    @Override
    public Class<GjtVersion> annotationClass() {
        return GjtVersion.class;
    }

    @Override
    public boolean canApplyToClassOnly() {
        return true;
    }
    
    public static final Class<Version> APPLIED_ANNOTATION_CLASS = Version.class;
    
    public static final ClassNode APPLIED_ANNOTATION_CLASS_NODE = make(APPLIED_ANNOTATION_CLASS);
    
    protected static final ClassNode DEFAULT_TYPE_CLASS_NODE = make(GjtUtils.getAnnotationMemberDefault(GjtVersion.class, "type", Class.class));
    
    protected static final String DEFAULT_PROPERTY_NAME = GjtUtils.getAnnotationMemberDefault(GjtVersion.class, "name", String.class);
    
    @Override
    protected void doVisit(final AnnotationNode annotationNode, final AnnotatedNode annotatedNode) {
        ClassNode entityClass = (ClassNode) annotatedNode;
        
        String fieldName = GjtUtils.getAnnotationMemberStringValue(annotationNode, "name");
        ClassNode fieldType = getMemberClassValue(annotationNode, "type", DEFAULT_TYPE_CLASS_NODE);
        
        FieldNode existingVersionField = entityClass.getField(fieldName);
        
        // don't add the version field if it already exists on this class or is visible from a superclass
        if (existingVersionField != null) {
            if(existingVersionField.getOwner().equals(entityClass)) {
                addError("Class annotated with GjtVersion cannot have a field named " + fieldName + " declared", existingVersionField);
            } else if (!Modifier.isPrivate(existingVersionField.getModifiers())) {
                addError("Class annotated with GjtVersion cannot have a field named " + fieldName + " declared because the field exists in the parent class: " + existingVersionField.getOwner().getName(), existingVersionField);
            }
            
            return;
        }
        
        // error if there's already an @Version on the class
        List<AnnotatedNode> conflicts = GjtUtils.findAnnotatedMembers(entityClass, APPLIED_ANNOTATION_CLASS_NODE);
        if(!conflicts.isEmpty()) {
            conflicts.forEach(conflict -> addError("Class annotated with GjtVersion cannot have an existing @Version", conflict));
            return;
        }
        
        ConstantExpression fieldDefaultValue = isPrimitiveType(fieldType) ?
            new ConstantExpression(0, true) :
            new ConstantExpression(null);
        
        PropertyNode versionProperty = entityClass.addProperty(fieldName, ACC_PUBLIC, fieldType, fieldDefaultValue, null, null);
        FieldNode versionField = versionProperty.getField();
        versionField.setModifiers(ACC_PRIVATE);
        versionField.addAnnotation(new AnnotationNode(APPLIED_ANNOTATION_CLASS_NODE));
    }
}
