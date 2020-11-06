package com.labijie.infra.telemetry.tracing.export

import com.labijie.infra.spring.configuration.getApplicationName
import com.labijie.infra.telemetry.configuration.TelemetryAutoConfiguration
import com.labijie.infra.telemetry.configuration.tracing.TracingProperties
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.trace.Span
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.config.ConfigException
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import java.time.Duration
import java.util.*


open class KafkaSpanExporter(
    protected val environment: Environment,
    protected val tracingProperties: TracingProperties
) : AbstractOltpSpanExporter() {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaSpanExporter::class.java)

        private fun Properties.propsToKafkaMap(): MutableMap<String, Any> {
            val map: MutableMap<String, Any> = mutableMapOf()
            for ((key, value) in this) {
                val k = (key as? String) ?:throw ConfigException(key.toString(), value, "Key must be a string.")
                if (k in ProducerConfig.configNames()) {
                    map[k] = this.getProperty(k)
                }
            }
            return map
        }
    }

    private val topic: String = tracingProperties.processorProperties.getProperty("topic", "telemetry-spans").toString()

    init {
        if (!tracingProperties.processorProperties.contains("topic")) {
            logger.warn("Kafka trace exporter missed property 'topic' for kafka exporter:  'telemetry-spans' be used.")
        }
    }


    private val kafkaProducer = createProducer()

    private fun createProducer(): KafkaProducer<String, ByteArray> {
        val ps = tracingProperties.processorProperties.propsToKafkaMap()
        ps.checkKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)
        if (ps.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG, environment.getApplicationName(false)) != null) {
            logger.warn("Kafka trace exporter missed property '${ProducerConfig.CLIENT_ID_CONFIG}' for kafka exporter:  'infra-telemetry-exporter' be used.")
        }

        ps.putIfAbsent(ProducerConfig.ACKS_CONFIG, "0")
        ps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        ps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
        return KafkaProducer(ps)
    }

    private fun Map<String, Any>.checkKey(key: String) {
        if (!this.containsKey(key)) {
            val str = StringBuilder()
            str.appendLine("${TelemetryAutoConfiguration.TracingPropertiesConfigurationKey} missed property '$key' for kafka exporter.")
            str.appendLine("Other kafka configuration reference: ")
            str.appendLine("https://kafka.apache.org/documentation/#producerconfigs")
            throw RuntimeException(str.toString())
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