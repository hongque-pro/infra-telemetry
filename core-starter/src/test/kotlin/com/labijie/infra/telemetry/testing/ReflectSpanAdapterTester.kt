package com.labijie.infra.telemetry.testing

import com.labijie.infra.telemetry.proto.ReflectSpanAdapter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReflectSpanAdapterTester {

    @Test
    fun testReflect(){
       val data =  ReflectSpanAdapter.toProtoResourceSpans(listOf())
        Assertions.assertNotNull(data)
    }
}