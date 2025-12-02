package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Condition
import org.powbot.api.script.tree.TreeScript
import kotlin.random.Random

/**
 * Helpers for consistent script-level actions (stop with logging, etc.).
 */
object ScriptUtils {

    /**
 * Log an informational message and stop the given script.
 */
    fun stopWithInfo(
        script: TreeScript,
        message: String,
        delayRangeMs: IntRange? = null
    ) {
        delayRangeMs?.let { range ->
            val delay = Random.nextInt(range.first, range.last + 1)
            Condition.sleep(delay)
        }
        Logger.info(message)
        script.controller.stop()
    }

    /**
 * Log an error message and stop the given script.
 */
    fun stopWithError(
        script: TreeScript,
        message: String,
        delayRangeMs: IntRange? = 1000..3000
    ) {
        delayRangeMs?.let { range ->
            val delay = Random.nextInt(range.first, range.last + 1)
            Condition.sleep(delay)
        }
        Logger.error(message)
        script.controller.stop()
    }
}
