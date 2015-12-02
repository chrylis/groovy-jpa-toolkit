package com.chrylis.gjt.transform;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import com.chrylis.gjt.annotation.TwoWaySetter;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class TwoWaySetterAstTransformation extends AbstractGjtAstTransformation<TwoWaySetter> {

    @Override
    public Class<TwoWaySetter> annotationClass() {
        return TwoWaySetter.class;
    }

    @Override
    public boolean canApplyToClassOnly() {
        return false;
    }

    protected final ClassNode MY_TYPE = annotationType();

    /**
     * Validates the structure of the bidirectional relationship and creates the code necessary for
     * setting the relationship from this side.
     * 
     * The algorithm implemented here is:
     * 
     * <ol>
     * <li>Sanity-check the annotated field (it backs a mutable reference property).
     * <li>Find the corresponding field representing the inverse side of the relationship.
     * <li>Confirm that the corresponding field points back here.
     * <li>Add a synthetic setter to be used by the corresponding class to avoid a setter loop.
     * <li>Create a public setter for this field's property that sets both sides of the relationship,
     * including clearing dangling relationships if any.
     * </ol>
     * 
     * Note in particular that this transformation apples only to this side of the relationship,
     * but it is required that the same transformation be applied to the other side independently.
     */
    @Override
    protected void doVisit(AnnotationNode annotationNode, AnnotatedNode annotatedNode) {
        FieldNode myField = (FieldNode) annotatedNode;
        if (!sane(myField)) {
            return;
        }

        FieldNode correspondingField = findCorrespondingField(myField);
        if (correspondingField == null) {
            return;
        }

        FieldNode inverse = findCorrespondingField(correspondingField);
        if (!myField.equals(inverse)) {
            StringBuilder sb = new StringBuilder("corresponding field ")
                .append(myField.getType().getNameWithoutPackage()).append('.').append(correspondingField.getName());

            if (inverse == null) {
                sb.append(" does not map to a field on this class");
            } else {
                sb.append(" is already mapped to a different relationship (")
                    .append(inverse.getName()).append("); the other field may need an explicit mapping");
            }

            addError(sb.toString(), myField);

            return;
        }

        addSyntheticSetterFor(myField);

        PropertyNode property = myField.getDeclaringClass().getProperty(myField.getName());
        property.setSetterBlock(createManagedSetterBody(myField, correspondingField));
    }

    protected static final int INTERESTING_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC | ACC_FINAL;
    protected static final int EXPECTED_MODIFIERS = ACC_PRIVATE;

    protected boolean sane(FieldNode field) {
        if ((field.getModifiers() & INTERESTING_MODIFIERS) != EXPECTED_MODIFIERS
            || field.getDeclaringClass().getProperty(field.getName()) == null) {

            addError(annotationName() + " must be applied to a mutable Groovy property", field);
            return false;
        }

        if (isPrimitiveType(field.getType())) {
            addError(annotationName() + " makes no sense on a primitive property", field);
            return false;
        }

        return true;
    }

    /**
     * Finds the "corresponding field" that is the other end of this field's bidirectional relationship.
     * 
     * This method checks to see whether the annotation on the field, which must be present, specifies
     * a {@code mappedBy} parameter; if so, then it returns the specified field. If not, it examines
     * all the fields on the corresponding class, returning a single matching field and raising an error
     * if zero or multiple fields match.
     * 
     * @param field
     *            the field whose correspondence is to be checked
     * @return the field on the other end of the bidirectional relationship, or {@code null} if no single matching field could be
     *         identified
     */
    protected FieldNode findCorrespondingField(FieldNode field) {
        // disregard entirely if this field is not annotated correctly, since we need to inspect the annotation
        List<AnnotationNode> twses = field.getAnnotations(MY_TYPE);
        if (twses.isEmpty()) {
            return null;
        }

        ClassNode correspondingClass = field.getType();

        // if the annotation specifies a "mappedBy" field, examine exactly that field
        String explicitFieldName = GjtUtils.getAnnotationMemberStringValue(twses.get(0), "mappedBy");
        if (!explicitFieldName.isEmpty()) {
            FieldNode specified = correspondingClass.getField(explicitFieldName);
            if (specified == null) {
                addError("the specified field \"" + explicitFieldName + "\" was not found on " + correspondingClass, field);
            } else if (specified.getAnnotations(MY_TYPE).isEmpty()) {
                addError("the corresponding field " + correspondingClass.getNameWithoutPackage() + "."
                    + specified.getName() + " is not annotated with " + annotationName(), field);
                specified = null;
            }
            return specified;
        }

        // if no field is specified, find all annotated fields on the corresponding class of the owning type
        ClassNode owningClass = field.getDeclaringClass();
        List<FieldNode> candidates = correspondingClass.getFields().stream()
            .filter(fn -> fn.getType().equals(owningClass))
            .filter(fn -> !fn.getAnnotations(MY_TYPE).isEmpty())
            .collect(Collectors.toList());

        // only successful path for unspecified corresponding field
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        StringBuilder sb = new StringBuilder("no explicit field name was provided and ");
        if (candidates.size() == 0) {
            sb.append("no matching fields were found");
        } else {
            sb.append(candidates.size()).append(" matching fields were found: ");
            sb.append(candidates.stream().map(FieldNode::getName).collect(Collectors.joining(",")));
        }
        addError(sb.toString(), field);

        return null;
    }

    protected void addSyntheticSetterFor(FieldNode field) {
        ClassNode declaringClass = field.getDeclaringClass();
        declaringClass.addMethod(
            syntheticSetterName(field),
            ACC_PUBLIC | ACC_FINAL | ACC_SYNTHETIC,
            ClassHelper.VOID_TYPE,
            new Parameter[] { setterParam(field) },
            new ClassNode[0],
            createSyntheticSetterBody(field));
    }

    protected static String syntheticSetterName(FieldNode field) {
        return "$gjt_" + GjtUtils.setterName(field);
    }

    protected static Parameter setterParam(FieldNode field) {
        return param(field.getType(), field.getName());
    }

    protected static Statement createSyntheticSetterBody(FieldNode field) {
        VariableExpression parameter = varX(setterParam(field));
        return assignS(fieldX(field), parameter);
    }

    protected static Statement createManagedSetterBody(FieldNode myField, FieldNode corresponding) {
        
        // The field on this class representing the relationship.
        Expression owningField = fieldX(myField);
        
        // The setter parameter (hard-set by Groovy as "value").
        Expression newCorrespondingObject = varX(param(myField.getType(), "value"));
        
        // Early exit if the parameter is already associated with this instance.
        Statement earlyExit = ifS(sameX(owningField, newCorrespondingObject), ReturnStatement.RETURN_NULL_OR_VOID);

        // If this object already has a relationship, null out the other side.
        Statement breakUpWithEx = ifS(
            notNullX(owningField),
            stmt(callX(owningField, syntheticSetterName(corresponding), ConstantExpression.NULL))
        );
        
        // Set this end of the relationship. There might not be another end.
        Statement iTakeYou = assignS(owningField, newCorrespondingObject);
        Statement exitIfNowSingle = (ifS(equalsNullX(newCorrespondingObject), ReturnStatement.RETURN_NULL_OR_VOID));
        
        // See if the new corresponding object already had a relationship. If so, null out the other end of that one.
        VariableExpression jilted = varX("jilted", corresponding.getType());
        Statement findJilted = declS(jilted, propX(newCorrespondingObject, corresponding.getName()));
        Statement stealSignificantOther = ifS(
            notNullX(jilted),
            assignS(attrX(jilted, constX(myField.getName())), constX(null))
        );

        // Set both ends of the new relationship.
        Statement youTakeMe = stmt(callX(newCorrespondingObject, syntheticSetterName(corresponding), varX("this")));

        return block(earlyExit, breakUpWithEx, iTakeYou, exitIfNowSingle, findJilted, stealSignificantOther, youTakeMe);
    }
}
