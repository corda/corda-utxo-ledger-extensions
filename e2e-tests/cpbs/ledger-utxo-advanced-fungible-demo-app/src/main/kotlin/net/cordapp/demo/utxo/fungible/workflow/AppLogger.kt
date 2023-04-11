package net.cordapp.demo.utxo.fungible.workflow

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

internal class AppLogger private constructor(private val logger: Logger) {
    companion object {
        inline fun <reified T : Any> create(): AppLogger {
            return AppLogger(LoggerFactory.getLogger(T::class.java))
        }
    }

    fun logBuildingTransaction() {
        log("Building transaction.")
    }

    fun logFinalizingTransaction() {
        log("Finalizing transaction.")
    }

    fun logReceivingFinalizedTransaction() {
        log("Receiving, signing and finalizing transaction.")
    }

    fun logMarshallingRequest(obj: Any) {
        log("Initializing request: $obj.")
    }

    fun log(message: String, level: Level = Level.INFO) {
        when (level) {
            Level.INFO -> logger.info(message)
            Level.DEBUG -> logger.debug(message)
            Level.ERROR -> logger.error(message)
            Level.WARN -> logger.warn(message)
            Level.TRACE -> logger.trace(message)
        }
    }
}