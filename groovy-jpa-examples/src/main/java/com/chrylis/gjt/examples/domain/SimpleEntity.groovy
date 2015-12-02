package com.chrylis.gjt.examples.domain

import groovy.transform.CompileStatic
import groovy.transform.NotYetImplemented;
import groovy.transform.ToString

import javax.persistence.Entity
import javax.persistence.ManyToMany

import com.chrylis.gjt.annotation.TwoWaySetter

@Entity
@CompileStatic
@ToString
class SimpleEntity {

    String contents = 'asdf'

    @TwoWaySetter(mappedBy = "bar")
    @ManyToMany
    OtherEntity foo
    
    SimpleEntity() {
        println 'in constructor'
    } 
    {
        println 'in initializer'
    }
    
    @NotYetImplemented
    public void asdf() {
        
    }
    
    public static void main(String[] args) {
        System.err.println 'asdf'
        new SimpleEntity().setFoo(new OtherEntity())
        System.err.println 'bar'
    }
}
