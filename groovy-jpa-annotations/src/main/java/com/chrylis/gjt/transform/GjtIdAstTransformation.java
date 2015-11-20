package com.chrylis.gjt.transform;

import static com.chrylis.gjt.transform.GjtUtils.getAnnotationMemberClassesValue;

import java.util.List;

import javax.persistence.Id;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.AnnotationCollectorTransform;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.GjtId;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GjtIdAstTransformation extends AbstractGjtPropertyAddingAstTransformation<GjtId> {

    private final AnnotationCollectorTransform collectorProcessor = new AnnotationCollectorTransform();
    
    @Override
    public Class<GjtId> annotationClass() {
        return GjtId.class;
    }

    @Override
    protected Class<?> identifyingAnnotation() {
        return Id.class;
    }
    
    @Override
    protected void doAddProperty(AnnotationNode annotation, ClassNode target, ClassNode type, String name) {
        PropertyNode property = addProperty(target, type, name);
        FieldNode field = property.getField();
        
        List<ClassNode> annotationCollectors = getAnnotationMemberClassesValue(annotation, "annotationCollectors");
        
        annotationCollectors.stream()
            .map(AnnotationNode::new)
            .map(collector -> collectorProcessor.visit(collector, collector, field, sourceUnit))
            .forEach(field::addAnnotations);
    }
}
