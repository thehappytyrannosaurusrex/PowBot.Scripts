package org.thehappytyrannosaurusrex.api.utils

import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Lightweight logging utility that wraps SLF4J with a console fallback.
 */
object Logger {

    // ------------------------------------------------------------------------
    // Internal setup
    // ------------------------------------------------------------------------

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val slf4jLogger = try {
        // Generic logger name so can be reused across all scripts
        LoggerFactory.getLogger("THTRxLOG")
    } catch (t: Throwable) {
        null // fallback mode
    }

    private fun timestamp(): String = sdf.format(Date())

    private enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    // ------------------------------------------------------------------------
    // Public API (scriptName + tag + message)
    // ------------------------------------------------------------------------

    fun debug(scriptName: String, tag: String, message: String) =
        log(Level.DEBUG, scriptName, tag, message, null)

    fun info(scriptName: String, tag: String, message: String) =
        log(Level.INFO, scriptName, tag, message, null)

    fun warn(scriptName: String, tag: String, message: String) =
        log(Level.WARN, scriptName, tag, message, null)

    fun error(scriptName: String, tag: String, message: String, throwable: Throwable? = null) =
        log(Level.ERROR, scriptName, tag, message, throwable)

    // ------------------------------------------------------------------------
    // Convenience overloads (no script/tag, or just script)
    // ------------------------------------------------------------------------

    /**
 * Generic info message with no script/tag context.
 */
    fun info(message: String) =
        log(Level.INFO, scriptName = null, tag = null, message = message, throwable = null)

    fun debug(message: String) =
        log(Level.DEBUG, scriptName = null, tag = null, message = message, throwable = null)

    fun warn(message: String) =
        log(Level.WARN, scriptName = null, tag = null, message = message, throwable = null)

    fun error(message: String, throwable: Throwable? = null) =
        log(Level.ERROR, scriptName = null, tag = null, message = message, throwable = throwable)

    /**
 * Convenience when want a scriptName but no tag:
 */
    fun infoForScript(scriptName: String, message: String) =
        log(Level.INFO, scriptName, tag = null, message = message, throwable = null)

    // ------------------------------------------------------------------------
    // Core implementation
    // ------------------------------------------------------------------------

    private fun format(scriptName: String?, tag: String?, message: String): String {
        val base = buildString {
            if (scriptName != null) {
                append("[$scriptName] ")
            }
            if (tag != null) {
                append("$tag | ")
            }
            append(message)
        }
        return base
    }

    private fun log(
        level: Level,
        scriptName: String?,
        tag: String?,
        message: String,
        throwable: Throwable?
    ) {
        val formattedMessage = format(scriptName, tag, message)

        // Try SLF4J first
        if (slf4jLogger != null) {
            try {
                when (level) {
                    Level.DEBUG -> {
                        if (throwable != null) slf4jLogger.debug(formattedMessage, throwable)
                        else slf4jLogger.debug(formattedMessage)
                    }
                    Level.INFO -> {
                        if (throwable != null) slf4jLogger.info(formattedMessage, throwable)
                        else slf4jLogger.info(formattedMessage)
                    }
                    Level.WARN -> {
                        if (throwable != null) slf4jLogger.warn(formattedMessage, throwable)
                        else slf4jLogger.warn(formattedMessage)
                    }
                    Level.ERROR -> {
                        if (throwable != null) slf4jLogger.error(formattedMessage, throwable)
                        else slf4jLogger.error(formattedMessage)
                    }
                }
                return
            } catch (_: Throwable) {
                // Fall through to console logging
            }
        }

        // Console fallback with timestamp + level
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
