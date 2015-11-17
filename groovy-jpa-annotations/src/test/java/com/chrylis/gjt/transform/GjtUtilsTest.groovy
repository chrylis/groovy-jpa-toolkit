package com.chrylis.gjt.transform

import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression

import spock.lang.Specification

import com.chrylis.gjt.annotation.GjtVersion

class GjtUtilsTest extends Specification {

    ClassNode cNode = new ClassNode(GjtVersion)
    AnnotationNode node = new AnnotationNode(cNode)
    
    def 'can retrieve default value for annotation node member'() {
        expect:
            'version' == GjtUtils.getAnnotationMemberDefault(node, 'name', String)
            Long == GjtUtils.getAnnotationMemberDefault(node, 'type', Class)
            
        when:
            GjtUtils.getAnnotationMemberDefault(node, 'invalid', String)
            
        then:
            IllegalArgumentException ex = thrown()
            ex.message.contains('invalid')
    }
    
    def 'can retrieve default value for annotation type member'() {
        expect:
            'version' == GjtUtils.getAnnotationMemberDefault(GjtVersion, "name", String)
            Long == GjtUtils.getAnnotationMemberDefault(GjtVersion, "type", Class)
    }
    
    def 'can retrieve String value or default for annotation node member'() {
        given:
            def fieldName = RandomStringUtils.randomAlphabetic(12)
        
        expect:
            'version' == GjtUtils.getAnnotationMemberStringValue(node, 'name')

        when:
            node.addMember('name', new ConstantExpression(fieldName))
            
        then:
            fieldName == GjtUtils.getAnnotationMemberStringValue(node, 'name')
    }
}
