package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Condition
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.inventory.RunePouch

object MagicUtil {

    // Represents cost of a spell for a single rune type
    data class RuneCost(val runeId: Int, val perCast: Int) {
        init { require(perCast > 0) { "perCast must be > 0 for runeId=$runeId" } }
    }

    // -------------------------------------------------------------------------
    // Spellbook State
    // -------------------------------------------------------------------------

    fun isSpellbookOpen(): Boolean {
        val magic = WidgetIds.SpellbookResizable.root()
        return magic.valid() && magic.visible()
    }

    fun isMagicTabSelected(): Boolean = Game.tab() == Game.Tab.MAGIC

    fun ensureSpellbookOpen(requireOverlay: Boolean = true): Boolean {
        if (!requireOverlay && isMagicTabSelected()) return true
        if (requireOverlay && isSpellbookOpen()) return true

        if (!Game.tab(Game.Tab.MAGIC)) return false

        return if (!requireOverlay) {
            Condition.wait({ isMagicTabSelected() }, 150, 10)
        } else {
            Condition.wait({ isSpellbookOpen() }, 150, 10)
        }
    }

    fun isReadyToCast(
        spell: Magic.MagicSpell,
        ensureTab: Boolean = true,
        requireOverlay: Boolean = true
    ): Boolean {
        if (ensureTab && !ensureSpellbookOpen(requireOverlay)) return false
        return spell.canCast()
    }

    fun currentBook(): Magic.Book = Magic.book()
    fun isOnBook(book: Magic.Book): Boolean = Magic.book() == book
    fun isOnStandard(): Boolean = isOnBook(Magic.Book.MODERN)
    fun isOnAncient(): Boolean = isOnBook(Magic.Book.ANCIENT)
    fun isOnLunar(): Boolean = isOnBook(Magic.Book.LUNAR)
    fun isOnArceuus(): Boolean = isOnBook(Magic.Book.ARCEUUS)

    // -------------------------------------------------------------------------
    // Rune Calculations
    // -------------------------------------------------------------------------

    fun castsRemaining(runeCounts: Map<Int, Int>, costs: Collection<RuneCost>): Int {
        if (costs.isEmpty()) return Int.MAX_VALUE

        var minCasts = Int.MAX_VALUE
        for (cost in costs) {
            val available = runeCounts[cost.runeId] ?: 0
            if (available <= 0) return 0
            val castsForRune = available / cost.perCast
            if (castsForRune < minCasts) minCasts = castsForRune
        }
        return if (minCasts == Int.MAX_VALUE) 0 else minCasts
    }

    fun castsRemaining(runeCounts: Map<Int, Int>, costs: Map<Int, Int>): Int =
        castsRemaining(runeCounts, costs.map { (id, perCast) -> RuneCost(id, perCast) })

    fun hasRunesForCast(runeCounts: Map<Int, Int>, costs: Collection<RuneCost>): Boolean =
        castsRemaining(runeCounts, costs) > 0

    fun runeShortfallForSingleCast(runeCounts: Map<Int, Int>, costs: Collection<RuneCost>): Map<Int, Int> {
        val missing = mutableMapOf<Int, Int>()
        for (cost in costs) {
            val have = runeCounts[cost.runeId] ?: 0
            if (have < cost.perCast) {
                missing[cost.runeId] = cost.perCast - have
            }
        }
        return missing
    }

    fun runeIdsFromCosts(costs: Collection<RuneCost>): Set<Int> =
        costs.mapTo(mutableSetOf()) { it.runeId }

    // -------------------------------------------------------------------------
    // Inventory Helpers
    // -------------------------------------------------------------------------

    fun inventoryRuneCounts(runeIds: Collection<Int>): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        runeIds.forEach { runeId ->
            val item = Inventory.stream().id(runeId).first()
            if (item.valid()) result[runeId] = item.stackSize()
        }
        return result
    }

    fun combineRuneCounts(vararg maps: Map<Int, Int>): Map<Int, Int> {
        val combined = mutableMapOf<Int, Int>()
        maps.forEach { source ->
            source.forEach { (runeId, amount) ->
                if (amount > 0) combined[runeId] = (combined[runeId] ?: 0) + amount
            }
        }
        return combined
    }

    // -------------------------------------------------------------------------
    // RunePouch Integration
    // -------------------------------------------------------------------------

    fun castsFromPouchOnly(fillResult: RunePouch.FillResult, costs: Collection<RuneCost>): Int =
        castsRemaining(fillResult.runeAmounts, costs)

    fun castsFromPouchAndInventory(
        fillResult: RunePouch.FillResult,
        costs: Collection<RuneCost>,
        includeInventory: Boolean = true
    ): Int {
        val pouchCounts = fillResult.runeAmounts
        if (!includeInventory) return castsRemaining(pouchCounts, costs)

        val runeIds = runeIdsFromCosts(costs)
        val inventoryCounts = inventoryRuneCounts(runeIds)
        val total = combineRuneCounts(pouchCounts, inventoryCounts)

        return castsRemaining(total, costs)
    }
}