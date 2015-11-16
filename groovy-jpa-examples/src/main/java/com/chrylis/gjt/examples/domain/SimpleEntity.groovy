package com.chrylis.gjt.examples.domain

import javax.persistence.Entity

import com.chrylis.gjt.annotation.GjtVersion

@GjtVersion
@Entity
class SimpleEntity {
    String contents
}
