package org.thehappytyrannosaurusrex.api.pathing

import org.powbot.dax.api.DaxWalker
import org.powbot.dax.teleports.Teleport
import org.thehappytyrannosaurusrex.api.utils.Logger

object DaxUtils {

    /**
     * Apply the default teleport blacklist (all minigame teleports).
     */
    fun applyDefaultBlacklist() {
        applyBlacklist(DaxConfig.BLACKLISTED_TELEPORTS.toList())
    }

    /**
     * Apply the default teleport blacklist, excluding specific teleports.
     * Use this when a script needs to use a specific minigame teleport.
     *
     * @param exclude Teleports to NOT blacklist (allow these)
     */
    fun applyBlacklistExcluding(vararg exclude: Teleport) {
        val excludeSet = exclude.toSet()
        val filtered = DaxConfig.BLACKLISTED_TELEPORTS.filter { it !in excludeSet }
        applyBlacklist(filtered)
    }

    /**
     * Apply the default teleport blacklist, excluding specific teleports.
     * Use this when a script needs to use specific minigame teleports.
     *
     * @param exclude Set of teleports to NOT blacklist (allow these)
     */
    fun applyBlacklistExcluding(exclude: Set<Teleport>) {
        val filtered = DaxConfig.BLACKLISTED_TELEPORTS.filter { it !in exclude }
        applyBlacklist(filtered)
    }

    /**
     * Apply a custom teleport blacklist.
     *
     * @param teleports List of teleports to blacklist
     */
    fun applyBlacklist(teleports: List<Teleport>) {
        try {
            if (teleports.isEmpty()) {
                Logger.info("[Pathing] No teleports to blacklist.")
                return
            }
            DaxWalker.blacklistTeleports(*teleports.toTypedArray())
            Logger.info("[Pathing] Applied DaxWalker teleport blacklist (${teleports.size} teleports).")
        } catch (e: Exception) {
            Logger.error("[Pathing] Failed to apply teleport blacklist: ${e.message}")
        }
    }

    /**
     * Apply a custom teleport blacklist from varargs.
     *
     * @param teleports Teleports to blacklist
     */
    fun applyBlacklist(vararg teleports: Teleport) {
        applyBlacklist(teleports.toList())
    }

    /**
     * Clear all teleport blacklists.
     */
    fun clearBlacklist() {
        try {
            DaxWalker.blacklistTeleports()
            Logger.info("[Pathing] Cleared DaxWalker teleport blacklist.")
        } catch (e: Exception) {
            Logger.error("[Pathing] Failed to clear teleport blacklist: ${e.message}")
        }
    }
}