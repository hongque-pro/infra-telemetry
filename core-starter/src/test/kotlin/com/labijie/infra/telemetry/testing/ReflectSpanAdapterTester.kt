package com.labijie.infra.telemetry.testing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReflectSpanAdapterTester {

    @Test
    fun testReflect(){
       val data =  SpanAdapter.toProtoResourceSpans(listOf())
        Assertions.assertNotNull(data)
    }
}