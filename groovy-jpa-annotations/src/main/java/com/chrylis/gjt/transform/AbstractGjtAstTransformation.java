package com.chrylis.gjt.transform;

import static org.codehaus.groovy.ast.ClassHelper.make;

import java.lang.annotation.Annotation;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;

public abstract class AbstractGjtAstTransformation<T extends Annotation> extends AbstractASTTransformation {

    /**
     * @return The annotation that triggers this AST transformation.
     */
    public abstract Class<T> annotationClass();


    /**
     * @return The triggering annotation type as a Groovy AST {@link ClassNode}.
     */
    public final ClassNode annotationType() {
        return make(annotationClass());
    }

    /**
     * @return The annotation in the form usually applied to a target (such as {@code @GjtFoo}).
     */
    public final String annotationName() {
        return "@" + annotationType().getNameWithoutPackage();
    }
    
    /**
     * @return Whether this transformation that has a target of {@code TYPE} can be applied only
     * to classes or also to interfaces.  
     */
    public abstract boolean canApplyToClassOnly();
    
    /**
     * Adds a compile error and returns true if the target class type is an interface.
     * Used by transformations that should only be applied to classes, such as ones
     * that manipulate properties.
     * 
     * @param targetNode the node that the annotation was applied to
     * @return {@code true} if the target node is an interface
     */
    protected final boolean targetNodeIsInterface(ClassNode targetNode) {
        return !checkNotInterface(targetNode, annotationName());
    }
    
    @Override
    public final void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        
        AnnotationNode annotationNode = (AnnotationNode) nodes[0];
        if(!annotationType().equals(annotationNode.getClassNode())) return;
        
        AnnotatedNode targetNode = (AnnotatedNode) nodes[1];
        
        if(canApplyToClassOnly()) {
            if(!(targetNode instanceof ClassNode)) {
                // This shouldn't happen because the annotation should specify a correct @Target.
                addError("Annotation " + annotationName() + " should only be applicable to classes, but it was applied to a non-type element.", annotationNode);
                return;
            } else if(targetNodeIsInterface((ClassNode) targetNode)) {
                // The class-only annotation was applied to an interface.
                return;
            }
        }
        
        doVisit(annotationNode, targetNode);
    }
    
    protected abstract void doVisit(AnnotationNode annotationNode, AnnotatedNode annotatedNode);
}
