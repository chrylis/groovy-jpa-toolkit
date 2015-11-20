package com.chrylis.gjt.transform.annotationcollectors

import groovy.transform.AnnotationCollector

import javax.persistence.Column

@AnnotationCollector
@Column(name = "foobar")
@interface AddColumnName {
}
