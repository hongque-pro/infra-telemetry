package com.labijie.infra.telemetry

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import kotlin.system.exitProcess

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2018-11-20
 */
class TelemetryBootstrapRunner : CommandLineRunner, ApplicationContextAware {
    private lateinit var applicationContext:ApplicationContext

    companion object{
        private val logger by lazy {
            LoggerFactory.getLogger(TelemetryBootstrapRunner::class.java)
        }
    }

    override fun setApplicationContext(p0: ApplicationContext) {
        this.applicationContext = p0
    }

    override fun run(vararg args: String?) {
        try {
            val initializers = this.applicationContext.getBeanProvider(ITelemetryInitializer::class.java)
            initializers.orderedStream().forEach {
                it.initialize()
            }
        }catch (ex:Throwable){
            logger.error("Infra telemetry service initailize fault.", ex)
            exitProcess(-9999)
        }
        logger.info("Infra telemetry service was initialized.")
    }
}