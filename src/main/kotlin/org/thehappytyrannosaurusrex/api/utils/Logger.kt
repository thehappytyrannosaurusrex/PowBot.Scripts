/*
 * Project: Arceuus Library Script (PowBot)
 * File: Logger.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.api.utils
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date

object Logger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val slf4jLogger = try {
        LoggerFactory.getLogger("ArceuusLibrary")
    } catch (t: Throwable) {
        null // fallback mode
    }

    private fun timestamp(): String = sdf.format(Date())

    fun info(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.info(message)
                return
            } catch (t: Throwable) {
                // fallback on failure
            }
        }
        println("[${timestamp()}] $message")
    }

    fun debug(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.debug(message)
                return
            } catch (t: Throwable) { }
        }
        Logger.info("[${timestamp()}] [DEBUG] $message")
    }

    fun warn(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.warn(message)
                return
            } catch (t: Throwable) {
                // fallback on failure
            }
        }
        println("[${timestamp()}] [WARN] $message")
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (slf4jLogger != null) {
            try {
                if (throwable != null) slf4jLogger.error(message, throwable)
                else slf4jLogger.error(message)
                return
            } catch (t: Throwable) { }
        }
        System.err.println("[${timestamp()}] [ERROR] $message")
        throwable?.printStackTrace()
    }
}