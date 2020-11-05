package com.labijie.infra.telemetry

import com.labijie.infra.utils.logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

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
            this.logger.error("Tracking service initailize fault.", ex)
            System.exit(-9999)
        }
        this.logger.info("Tracking service was initialized.")
    }
}