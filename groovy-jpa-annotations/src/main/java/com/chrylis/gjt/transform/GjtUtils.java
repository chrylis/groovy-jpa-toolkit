package com.chrylis.gjt.transform;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.transform.AbstractASTTransformation;

public final class GjtUtils {
    
    private GjtUtils() {
    }

    public static List<FieldNode> findAnnotatedFields(ClassNode target, ClassNode annotationClass) {
        return target.getFields().stream()
            .filter(field -> !field.getAnnotations(annotationClass).isEmpty())
            .collect(Collectors.toList());
    }
    
    public static List<PropertyNode> findAnnotatedProperties(ClassNode target, ClassNode annotationClass) {
        return target.getProperties().stream()
            .filter(property -> !property.getAnnotations(annotationClass).isEmpty())
            .collect(Collectors.toList());
    }
    
    public static List<MethodNode> findAnnotatedMethods(ClassNode target, ClassNode annotationClass) {
        return target.getMethods().stream()
            .filter(method -> !method.getAnnotations(annotationClass).isEmpty())
            .collect(Collectors.toList());
    }
    
    public static List<AnnotatedNode> findAnnotatedMembers(ClassNode target, ClassNode annotationClass) {
        List<AnnotatedNode> result = new ArrayList<>();
        result.addAll(findAnnotatedFields(target, annotationClass));
        result.addAll(findAnnotatedProperties(target, annotationClass));
        result.addAll(findAnnotatedMethods(target, annotationClass));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationMemberDefault(AnnotationNode node, String name, Class<T> memberType) {
        Class<?> nodeClass = node.getClassNode().getTypeClass();
        if(!nodeClass.isAnnotation()) {
            throw new IllegalArgumentException("AnnotationNode type " + nodeClass + " is not an annotation type");
        }
        
        // safe because we checked above
        return getAnnotationMemberDefault((Class<? extends Annotation>) nodeClass, name, memberType);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationMemberDefault(Class<? extends Annotation> annotationType, String name, Class<T> memberType) {
        if(!annotationType.isAnnotation()) {
            throw new IllegalArgumentException("Annotation type " + annotationType + " is not an annotation type");
        }
        
        try {
            Method method = annotationType.getMethod(name);
            return (T) method.getDefaultValue();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("member " + name + " does not exist on annotation type " + annotationType);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }
        
    public static String getAnnotationMemberStringValue(AnnotationNode node, String name) {
        String value = AbstractASTTransformation.getMemberStringValue(node, name);
        if(value == null) {
            value = getAnnotationMemberDefault(node, name, String.class);
        }
        return value;
    }
}
