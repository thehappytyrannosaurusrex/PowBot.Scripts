package org.thehappytyrannosaurusrex.api.pathing

import org.powbot.dax.api.DaxWalker
import org.thehappytyrannosaurusrex.api.utils.Logger

/**
 * Shared helpers for configuring DaxWalker across scripts.
 */
object DaxUtils {

    /**
 * Apply the default teleport blacklist defined in DaxConfig.BLACKLISTED_TELEPORTS.
 */
    fun applyDefaultBlacklist() {
        try {
            DaxWalker.blacklistTeleports(*DaxConfig.BLACKLISTED_TELEPORTS)
            Logger.info("[Pathing] Applied DaxWalker teleport blacklist.")
        } catch (e: Exception) {
            Logger.error("[Pathing] Failed to apply teleport blacklist: ${e.message}")
        }
    }
}
