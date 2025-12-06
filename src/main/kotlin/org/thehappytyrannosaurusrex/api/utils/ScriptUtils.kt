package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.api.script.tree.TreeScript
import org.powbot.mobile.script.ScriptManager
import kotlin.random.Random

object ScriptUtils {

    fun stopWithInfo(script: TreeScript, message: String, delayRangeMs: IntRange? = null) {
        delayRangeMs?.let { Condition.sleep(Random.nextInt(it.first, it.last + 1)) }
        Logger.info(message)
        Notifications.showNotification(message)
        ScriptManager.stop()
    }

    fun stopWithError(script: TreeScript, message: String, delayRangeMs: IntRange? = 1000..3000) {
        delayRangeMs?.let { Condition.sleep(Random.nextInt(it.first, it.last + 1)) }
        Logger.error(message)
        Notifications.showNotification(message)
        ScriptManager.stop()
    }
}