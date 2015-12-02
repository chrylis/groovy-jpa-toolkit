package com.chrylis.gjt.transform;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.GjtTest;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class GjtTestAstTransformation extends AbstractGjtAstTransformation<GjtTest> {
    
    @Override
    public Class<GjtTest> annotationClass() {
        return GjtTest.class;
    }

    @Override
    public boolean canApplyToClassOnly() {
        return false;
    }

    @Override
    protected void doVisit(AnnotationNode annotationNode, AnnotatedNode annotatedNode) {
        FieldNode f = (FieldNode) annotatedNode;
        PropertyNode p = f.getDeclaringClass().getProperty(f.getName());
        System.err.println("\n\n"+f.getModifiers()+"\n\n");
        
        p.setSetterBlock(stmt(callX(fieldX(make(System.class), "err"), "println", new ConstantExpression("foobar"))));
    }
}
