package com.chrylis.gjt.transform

import groovy.transform.CompileStatic

import java.lang.reflect.Field

import javax.persistence.Version

import spock.lang.Specification

import com.chrylis.gjt.annotation.GjtVersion

class GjtVersionAstTransformationTest extends Specification {

    @GjtVersion
    @CompileStatic
    class Versioned {
    }
    
    def 'annotated class has version field'() {
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
}
