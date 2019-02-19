package com.jacekmarchwicki.logsmanager.internal

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class RemovableInRelease