package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.Condition
import org.thehappytyrannosaurusrex.api.inventory.RunePouch
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Quests
import org.thehappytyrannosaurusrex.api.data.WidgetIds

/**
 * Reusable magic-related utilities.
 */
object MagicUtil {

    /**
 * Represents the cost of a spell in terms of a single rune type.
 */
    data class RuneCost(
        val runeId: Int,
        val perCast: Int
    ) {
        init {
            require(perCast > 0) { "perCast must be > 0 for runeId=$runeId" }
        }
    }



    // ------------------------------------------------------------------------
    // Spellbook helpers
    // ------------------------------------------------------------------------

    /**
 * Returns true if the spellbook overlay (resizable side-pane) is visible.
 */
    fun isSpellbookOpen(): Boolean {
        val magic = WidgetIds.SpellbookResizable.ROOT.component()
        if (!magic.valid()) {
            return false
        }

        return magic.visible()
    }

    /**
 * Returns true if the game is currently on the magic tab.
 */
    fun isMagicTabSelected(): Boolean =
        Game.tab() == Game.Tab.MAGIC

    /**
 * Ensures the magic tab is open and, optionally, that the spellbook overlay
 */
    fun ensureSpellbookOpen(
        requireOverlay: Boolean = true
    ): Boolean {
        // Already open to satisfaction?
        if (!requireOverlay && isMagicTabSelected()) return true
        if (requireOverlay && isSpellbookOpen()) return true

        // Switch to magic tab
        if (!Game.tab(Game.Tab.MAGIC)) {
            return false
        }

        if (!requireOverlay) {
            // Just care that the tab is selected.
            return Condition.wait(
                { isMagicTabSelected() },
                150,
                10
            )
        }

        // Wait for actual overlay to appear
        return Condition.wait(
            { isSpellbookOpen() },
            150,
            10
        )
    }

    /**
 * Returns true if the given [spell] is "ready" according to PowBot AND,
 */
    fun isReadyToCast(
        spell: Magic.MagicSpell,
        ensureTab: Boolean = true,
        requireOverlay: Boolean = true
    ): Boolean {
        if (ensureTab && !ensureSpellbookOpen(requireOverlay)) {
            return false
        }
        return Magic.ready(spell)
    }

    /**
 * Returns the current magic spellbook.
 */
    fun currentBook(): Magic.Book = Magic.book()

    /**
 * Checks whether the current spellbook matches [book].
 */
    fun isOnBook(book: Magic.Book): Boolean = Magic.book() == book

    /**
 * Convenience wrappers for common spellbooks.
 */
    fun isOnModern(): Boolean = isOnBook(Magic.Book.MODERN)
    fun isOnAncient(): Boolean = isOnBook(Magic.Book.ANCIENT)
    fun isOnLunar(): Boolean = isOnBook(Magic.Book.LUNAR)
    fun isOnArceuus(): Boolean = isOnBook(Magic.Book.ARCEUUS)

    /**
 * Returns a human-friendly name for the current spellbook,
 */
    fun currentBookName(): String = when (Magic.book()) {
        Magic.Book.MODERN  -> "Modern"
        Magic.Book.ANCIENT -> "Ancient"
        Magic.Book.LUNAR   -> "Lunar"
        Magic.Book.ARCEUUS -> "Arceuus"
        Magic.Book.NIL     -> "None"
    }

    /**
 * Do have permanent access to Ancient magicks?
 */
    fun hasAncientAccess(): Boolean =
        Quests.Quest.DESERT_TREASURE_I.completed()

    /**
 * Do have permanent access to the Lunar spellbook?
 */
    fun hasLunarAccess(): Boolean =
        Quests.Quest.LUNAR_DIPLOMACY.completed()

    // Can also add tiny convenience aliases if like:
    fun canUseAncient(): Boolean = hasAncientAccess()
    fun canUseLunar(): Boolean = hasLunarAccess()

    // ------------------------------------------------------------------------
    // Core rune / cast math
    // ------------------------------------------------------------------------

    /**
 * Compute how many casts can do, given:
 */
    fun castsRemaining(
        runeCounts: Map<Int, Int>,
        costs: Collection<RuneCost>
    ): Int {
        if (costs.isEmpty()) return 0

        var minCasts = Int.MAX_VALUE

        for (cost in costs) {
            val available = runeCounts[cost.runeId] ?: 0
            if (available <= 0) {
                // Missing a required rune -> 0 casts
                return 0
            }
            val castsForRune = available / cost.perCast
            if (castsForRune < minCasts) {
                minCasts = castsForRune
            }
        }

        return if (minCasts == Int.MAX_VALUE) 0 else minCasts
    }

    /**
 * Overload for convenience: [costs] as a map of runeId -> perCast.
 */
    fun castsRemaining(
        runeCounts: Map<Int, Int>,
        costs: Map<Int, Int>
    ): Int = castsRemaining(
        runeCounts,
        costs.map { (id, perCast) -> RuneCost(id, perCast) }
    )

    /**
 * Returns true if have enough runes for at least 1 cast with
 */
    fun hasRunesForCast(
        runeCounts: Map<Int, Int>,
        costs: Collection<RuneCost>
    ): Boolean = castsRemaining(runeCounts, costs) > 0

    /**
 * For diagnostics: returns how many additional runes of each type
 */
    fun runeShortfallForSingleCast(
        runeCounts: Map<Int, Int>,
        costs: Collection<RuneCost>
    ): Map<Int, Int> {
        val missing = mutableMapOf<Int, Int>()
        for (cost in costs) {
            val have = runeCounts[cost.runeId] ?: 0
            if (have < cost.perCast) {
                missing[cost.runeId] = cost.perCast - have
            }
        }
        return missing
    }

    /**
 * Collects all rune IDs mentioned in [costs].
 */
    fun runeIdsFromCosts(costs: Collection<RuneCost>): Set<Int> =
        costs.mapTo(mutableSetOf()) { it.runeId }

    // ------------------------------------------------------------------------
    // Inventory helpers
    // ------------------------------------------------------------------------

    /**
 * Reads rune amounts from inventory for the given [runeIds].
 */
    fun inventoryRuneCounts(runeIds: Collection<Int>): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()

        runeIds.forEach { runeId ->
            val item = Inventory.stream().id(runeId).first()
            if (item.valid()) {
                result[runeId] = item.stackSize()
            }
        }

        return result
    }

    /**
 * Merge multiple rune-count maps (pouch, inventory, etc.) into one.
 */
    fun combineRuneCounts(vararg maps: Map<Int, Int>): Map<Int, Int> {
        val combined = mutableMapOf<Int, Int>()
        maps.forEach { source ->
            source.forEach { (runeId, amount) ->
                if (amount > 0) {
                    combined[runeId] = (combined[runeId] ?: 0) + amount
                }
            }
        }
        return combined
    }

    // ------------------------------------------------------------------------
    // Helpers that work directly with RunePouch.FillResult
    // ------------------------------------------------------------------------

    /**
 * Compute casts remaining using only runes that were loaded into
 */
    fun castsFromPouchOnly(
        fillResult: RunePouch.FillResult,
        costs: Collection<RuneCost>
    ): Int = castsRemaining(fillResult.runeAmounts, costs)

    /**
 * Compute casts remaining using:
 */
    fun castsFromPouchAndInventory(
        fillResult: RunePouch.FillResult,
        costs: Collection<RuneCost>,
        includeInventory: Boolean = true
    ): Int {
        val pouchCounts = fillResult.runeAmounts

        if (!includeInventory) {
            return castsRemaining(pouchCounts, costs)
        }

        // Collect all rune IDs care about (from the costs).
        val runeIds = runeIdsFromCosts(costs)

        val inventoryCounts = inventoryRuneCounts(runeIds)
        val total = combineRuneCounts(pouchCounts, inventoryCounts)

        return castsRemaining(total, costs)
    }
}
