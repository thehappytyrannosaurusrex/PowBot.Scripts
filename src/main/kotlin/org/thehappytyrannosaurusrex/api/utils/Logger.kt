package org.thehappytyrannosaurusrex.api.utils

import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date

object Logger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val slf4jLogger = try { LoggerFactory.getLogger("THTRxLOG") } catch (_: Throwable) { null }

    private fun timestamp(): String = sdf.format(Date())

    private enum class Level { DEBUG, INFO, WARN, ERROR }

    // Primary API: [ScriptName] TAG | message
    fun debug(scriptName: String, tag: String, message: String) = log(Level.DEBUG, scriptName, tag, message, null)
    fun info(scriptName: String, tag: String, message: String) = log(Level.INFO, scriptName, tag, message, null)
    fun warn(scriptName: String, tag: String, message: String) = log(Level.WARN, scriptName, tag, message, null)
    fun error(scriptName: String, tag: String, message: String, throwable: Throwable? = null) = log(Level.ERROR, scriptName, tag, message, throwable)

    // Simple API (for utilities)
    fun info(message: String) = log(Level.INFO, null, null, message, null)
    fun debug(message: String) = log(Level.DEBUG, null, null, message, null)
    fun warn(message: String) = log(Level.WARN, null, null, message, null)
    fun error(message: String, throwable: Throwable? = null) = log(Level.ERROR, null, null, message, throwable)

    private fun log(level: Level, scriptName: String?, tag: String?, message: String, throwable: Throwable?) {
        val formatted = buildString {
            if (scriptName != null) append("[$scriptName] ")
            if (tag != null) append("$tag | ")
            append(message)
        }

        when (level) {
            Level.DEBUG -> slf4jLogger?.debug(formatted) ?: println("${timestamp()} [DEBUG] $formatted")
            Level.INFO -> slf4jLogger?.info(formatted) ?: println("${timestamp()} [INFO] $formatted")
            Level.WARN -> slf4jLogger?.warn(formatted) ?: println("${timestamp()} [WARN] $formatted")
            Level.ERROR -> {
                if (throwable != null) {
                    slf4jLogger?.error(formatted, throwable) ?: println("${timestamp()} [ERROR] $formatted\n${throwable.stackTraceToString()}")
                } else {
                    slf4jLogger?.error(formatted) ?: println("${timestamp()} [ERROR] $formatted")
                }
            }
        }
    }
}