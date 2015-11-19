package com.chrylis.gjt.transform

import groovy.transform.CompileStatic

import java.lang.reflect.Field
import java.time.Instant

import javax.persistence.Version

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.Message

import spock.lang.Specification

import com.chrylis.gjt.annotation.GjtVersion

class GjtVersionAstTransformationTest extends Specification {

    @GjtVersion
    @CompileStatic
    class Versioned {
    }
    
    def 'annotated class with default parameters has Long "version" field'() {
        given:
            Field field = Versioned.getDeclaredField('version')
            
        expect:
            Long == field.type
            field.getAnnotation(Version)
    }
    
    def 'getters and setters for version field work'() {            
        given:
            Long randomNumber = new Random().nextLong()
            
            def v = new Versioned()
            v.setVersion(randomNumber)
            
        expect:
            randomNumber == v.getVersion()
            randomNumber == v.@version
    }
    
    @CompileStatic
    Class makeVersionedClass(Class type, String name, String body = '') {
        new GroovyClassLoader().parseClass("""
            @groovy.transform.CompileStatic
            @com.chrylis.gjt.annotation.GjtVersion(type=$type.name, name="$name")
            class Temp { $body }""")
    }
    
    def 'parameters are applied correctly'(Class type, String fieldName) {
        given:
            Field field = makeVersionedClass(type, fieldName, 'Short version').getDeclaredField(fieldName)
        
        expect:
            type == field.type
            field.getAnnotation(Version)
            
        where:
            type    | fieldName
            int     | 'foobar'
            Long    | 'helloWorld'
            Instant | 'asdf'
    }
    
    def 'conflicting field names produce an error'() {
        when:
            makeVersionedClass(Long, 'version', 'Long version')
            
        then:
            MultipleCompilationErrorsException ex = thrown()
            ex.message.contains('@GjtVersion')
            ex.message.contains('already exists')
            
            1 == ex.errorCollector.errorCount
    }
    
    def 'conflicting annotation produces an error'() {
        when:
            makeVersionedClass(Long, 'version', '@javax.persistence.Version Integer foo')
            
        then:
            MultipleCompilationErrorsException ex = thrown()
            ex.message.contains('@GjtVersion')
            ex.message.contains('existing @Version')
            
            1 == ex.errorCollector.errorCount
    }
}
