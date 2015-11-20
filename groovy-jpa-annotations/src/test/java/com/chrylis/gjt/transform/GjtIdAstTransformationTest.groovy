package com.chrylis.gjt.transform

import groovy.transform.CompileStatic

import java.lang.reflect.Field
import java.time.Instant

import javax.persistence.Id

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import spock.lang.Specification

import com.chrylis.gjt.annotation.GjtId

class GjtIdAstTransformationTest extends Specification {

    @GjtId
    @CompileStatic
    class Identified {
    }
    
    def 'annotated class with default parameters has Long "id" field'() {
        given:
            Field field = Identified.getDeclaredField('id')
            
        expect:
            Long == field.type
            field.getAnnotation(Id)
    }
    
    def 'getters and setters for version field work'() {            
        given:
            Long randomNumber = new Random().nextLong()
            
            def v = new Identified()
            v.setId(randomNumber)
            
        expect:
            randomNumber == v.getId()
            randomNumber == v.@id
    }
    
    @CompileStatic
    Class makeIdentifiedClass(Class type, String name, String body = '') {
        new GroovyClassLoader().parseClass("""
            @groovy.transform.CompileStatic
            @com.chrylis.gjt.annotation.GjtId(type=$type.name, name="$name")
            class Temp { $body }""")
    }
    
    def 'parameters are applied correctly'(Class type, String fieldName) {
        given:
            Field field = makeIdentifiedClass(type, fieldName, 'Short id').getDeclaredField(fieldName)
        
        expect:
            type == field.type
            field.getAnnotation(Id)
            
        where:
            type    | fieldName
            int     | 'foobar'
            Long    | 'helloWorld'
            Instant | 'asdf'
    }
    
    def 'conflicting field names produce an error'() {
        when:
            makeIdentifiedClass(Long, 'id', 'Long id')
            
        then:
            MultipleCompilationErrorsException ex = thrown()
            ex.message.contains('@GjtId')
            ex.message.contains('already exists')
            
            1 == ex.errorCollector.errorCount
    }
    
    def 'conflicting annotation produces an error'() {
        when:
            makeIdentifiedClass(Long, 'id', '@javax.persistence.Id Integer foo')
            
        then:
            MultipleCompilationErrorsException ex = thrown()
            ex.message.contains('@GjtId')
            ex.message.contains('existing @Id')
            
            1 == ex.errorCollector.errorCount
    }
}
