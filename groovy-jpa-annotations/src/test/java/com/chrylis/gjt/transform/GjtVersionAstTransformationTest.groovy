package com.chrylis.gjt.transform

import groovy.transform.CompileStatic

import java.lang.reflect.Field
import java.time.Instant

import javax.persistence.Version

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
    Class makeVersionedClass(Class type, String name) {
        new GroovyClassLoader().parseClass("""
            @groovy.transform.CompileStatic
            @com.chrylis.gjt.annotation.GjtVersion(type=$type.name, name="$name")
            class Temp {}""")
    }
    
    def 'parameters are applied correctly'(Class type, String fieldName) {
        given:
            Field field = makeVersionedClass(type, fieldName).getDeclaredField(fieldName)
        
        expect:
            type == field.type
            field.getAnnotation(Version)
            
        where:
            type    | fieldName
            int     | 'foobar'
            Long    | 'helloWorld'
            Instant | 'asdf'
    }
}
