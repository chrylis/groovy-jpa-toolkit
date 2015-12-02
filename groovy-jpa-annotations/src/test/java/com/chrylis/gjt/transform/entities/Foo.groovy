package com.chrylis.gjt.transform.entities

import groovy.transform.CompileStatic

import com.chrylis.gjt.annotation.TwoWaySetter

@CompileStatic
class Foo {
    @TwoWaySetter
    Bar bar
}
