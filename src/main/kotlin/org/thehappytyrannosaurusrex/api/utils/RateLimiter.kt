package org.thehappytyrannosaurusrex.api.utils

/**
 * Simple time-based rate limiter.
 */
class RateLimiter(private val minIntervalMs: Long) {
    private var lastRunAt: Long = 0L

    /**
 * Returns true if enough time has passed since the last successful run.
 */
    fun shouldRun(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastRunAt >= minIntervalMs) {
            lastRunAt = now
            return true
        }
        return false
    }

    /**
 * Force the next call to [shouldRun] to succeed immediately, regardless
 */
    fun reset() {
        lastRunAt = 0L
    }
}
