package org.thehappytyrannosaurusrex.api.utils

// Simple time-based rate limiter
class RateLimiter(private val minIntervalMs: Long) {
    private var lastRunAt: Long = 0L

    // Returns true if enough time has passed since last run
    fun shouldRun(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastRunAt >= minIntervalMs) {
            lastRunAt = now
            return true
        }
        return false
    }

    fun reset() {
        lastRunAt = 0L
    }
}