package org.thehappytyrannosaurusrex.api.utils

import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date

// Lightweight logging utility wrapping SLF4J with console fallback
object Logger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val slf4jLogger = try {
        LoggerFactory.getLogger("THTRxLOG")
    } catch (t: Throwable) {
        null
    }

    private fun timestamp(): String = sdf.format(Date())

    private enum class Level { DEBUG, INFO, WARN, ERROR }

    // --- Public API (scriptName + tag + message) ---

    fun debug(scriptName: String, tag: String, message: String) =
        log(Level.DEBUG, scriptName, tag, message, null)

    fun info(scriptName: String, tag: String, message: String) =
        log(Level.INFO, scriptName, tag, message, null)

    fun warn(scriptName: String, tag: String, message: String) =
        log(Level.WARN, scriptName, tag, message, null)

    fun error(scriptName: String, tag: String, message: String, throwable: Throwable? = null) =
        log(Level.ERROR, scriptName, tag, message, throwable)

    // --- Convenience overloads (no script/tag) ---

    fun info(message: String) = log(Level.INFO, null, null, message, null)
    fun debug(message: String) = log(Level.DEBUG, null, null, message, null)
    fun warn(message: String) = log(Level.WARN, null, null, message, null)
    fun error(message: String, throwable: Throwable? = null) = log(Level.ERROR, null, null, message, throwable)

    fun infoForScript(scriptName: String, message: String) =
        log(Level.INFO, scriptName, null, message, null)

    // --- Core implementation ---

    private fun format(scriptName: String?, tag: String?, message: String): String = buildString {
        if (scriptName != null) append("[$scriptName] ")
        if (tag != null) append("$tag | ")
        append(message)
    }

    private fun log(level: Level, scriptName: String?, tag: String?, message: String, throwable: Throwable?) {
        val formattedMessage = format(scriptName, tag, message)

        if (slf4jLogger != null) {
            try {
                when (level) {
                    Level.DEBUG -> if (throwable != null) slf4jLogger.debug(formattedMessage, throwable) else slf4jLogger.debug(formattedMessage)
                    Level.INFO -> if (throwable != null) slf4jLogger.info(formattedMessage, throwable) else slf4jLogger.info(formattedMessage)
                    Level.WARN -> if (throwable != null) slf4jLogger.warn(formattedMessage, throwable) else slf4jLogger.warn(formattedMessage)
                    Level.ERROR -> if (throwable != null) slf4jLogger.error(formattedMessage, throwable) else slf4jLogger.error(formattedMessage)
                }
                return
            } catch (_: Throwable) {
                // Fall through to console
            }
        }

        // Console fallback
        val line = "[${timestamp()}] [${level.name}] $formattedMessage"
        when (level) {
            Level.ERROR -> {
                System.err.println(line)
                throwable?.printStackTrace()
            }
            else -> println(line)
        }
    }
}