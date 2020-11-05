package com.labijie.infra.telemetry.tracing.export

import com.labijie.infra.telemetry.configuration.TelemetryAutoConfiguration
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.sdk.common.CompletableResultCode
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.lang.StringBuilder
import java.time.Duration
import java.util.*


open class KafkaSpanExporter(
    protected val environment: Environment,
    protected val tracingProperties: TracingProperties
) : AbstractOltpSpanExporter() {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaSpanExporter::class.java)
    }

    private val properties: Properties by lazy {
       val ps = Properties(tracingProperties.processorProperties)
        ps.checkKey("bootstrap.servers")
        if (ps.putIfAbsent("client.id", "infra-trace-exporter") != null) {
            logger.warn("Kafka trace exporter missed property 'client.id' for kafka exporter:  'infra-telemetry-exporter' be used.")
        }
        if (ps.putIfAbsent("topic", "infra-trace-spans") != null) {
            logger.warn("Kafka trace exporter missed property 'topic' for kafka exporter:  'telemetry-spans' be used.")
        }
        properties.putIfAbsent("acks", "0")
        ps
    }

    private val topic: String by lazy {
        this.properties.getOrDefault("topic", "telemetry-spans").toString()
    }

    private val kafkaProducer = createProducer()

    private fun createProducer(): KafkaProducer<String, ByteArray> {
        return KafkaProducer(properties, StringSerializer(), ByteArraySerializer())
    }

    private fun Properties.checkKey(key: String) {
        if (!this.containsKey(key)) {
            val str = StringBuilder()
            str.appendLine("${TelemetryAutoConfiguration.TracingPropertiesConfigurationKey} missed property '$key' for kafka exporter.")
            str.appendLine("Kafka configuration reference: ")
            str.appendLine("https://kafka.apache.org/documentation/#producerconfigs")
            throw RuntimeException()
        }
    }

    override fun exportRequest(request: ExportTraceServiceRequest) {
        val record = ProducerRecord<String, ByteArray>(this.topic, request.toByteArray())
        kafkaProducer.send(record)
    }

    override fun flush(): CompletableResultCode {
        return CompletableResultCode.ofSuccess()
    }

    override fun shutdown(): CompletableResultCode {
         kafkaProducer.close(Duration.ofMinutes(1))
        return CompletableResultCode.ofSuccess()
    }
}