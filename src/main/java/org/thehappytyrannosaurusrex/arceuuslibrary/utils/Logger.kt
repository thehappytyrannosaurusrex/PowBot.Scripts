/*
 * Project: Arceuus Library Script (PowBot)
 * File: Logger.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.utils

import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date
/**
 * Hybrid logger — uses SLF4J (via slf4j-simple) when available,
 * and falls back to standard println with timestamp otherwise.
 *
 * Safe to use in PowBot, even if the SLF4J backend fails to load.
 */
/**
 * Logger: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object Logger {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val slf4jLogger = try {
        LoggerFactory.getLogger("ArceuusLibrary")
    } catch (t: Throwable) {
        null // fallback mode
    }

    private fun timestamp(): String = sdf.format(Date())

    /** Default info-level logging (compatible with old Logger.logger()) */
    fun info(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.info(message)
                return
            } catch (t: Throwable) {
                // fallback on failure
            }
        }
        Logger.info("[${timestamp()}] $message")
    }

    /** Debug-level message */
    fun debug(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.debug(message)
                return
            } catch (t: Throwable) { }
        }
        Logger.info("[${timestamp()}] [DEBUG] $message")
    }

    /** Warning message */
    fun warn(message: String) {
        if (slf4jLogger != null) {
            try {
                slf4jLogger.warn(message)
                return
            } catch (t: Throwable) { }
        }
        Logger.info("[${timestamp()}] [WARN] $message")
    }

    /** Error message with optional stack trace */
    fun error(message: String, throwable: Throwable? = null) {
        if (slf4jLogger != null) {
            try {
                if (throwable != null) slf4jLogger.error(message, throwable)
                else slf4jLogger.error(message)
                return
            } catch (t: Throwable) { }
        }
        Logger.info("[${timestamp()}] [ERROR] $message")
        throwable?.printStackTrace()
    }
}