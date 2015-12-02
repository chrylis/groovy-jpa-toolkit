package com.chrylis.gjt.examples.domain

import groovy.transform.CompileStatic

import javax.persistence.Entity
import javax.persistence.OneToOne

import com.chrylis.gjt.annotation.TwoWaySetter

@Entity
@CompileStatic
class OtherEntity {
    @TwoWaySetter(mappedBy = "foo")
    @OneToOne
    SimpleEntity bar
    
    Integer asdf
}
