package org.thehappytyrannosaurusrex.api.inventory

import com.google.common.eventbus.Subscribe
import org.powbot.api.Condition
import org.powbot.api.Events
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Game.Tab

// Utility for handling Rune pouch and Divine rune pouch
object RunePouch {

    const val RUNE_POUCH = 12791
    const val DIVINE_RUNE_POUCH = 27281
    const val CAPACITY_PER_RUNE = 16_000
    const val NORMAL_MAX_RUNES = 3
    const val DIVINE_MAX_RUNES = 4

    enum class Type(val id: Int, val maxRunes: Int) {
        NORMAL(RUNE_POUCH, NORMAL_MAX_RUNES),
        DIVINE(DIVINE_RUNE_POUCH, DIVINE_MAX_RUNES)
    }

    data class FillResult(
        val pouchType: Type,
        val runeAmounts: Map<Int, Int>
    ) {
        val totalRunes: Int get() = runeAmounts.values.sum()
    }

    private const val EMPTY_MESSAGE = "The pouch is empty."
    private const val WAIT_INTERVAL_SHORT = 150
    private const val WAIT_INTERVAL_LONG = 200
    private const val WAIT_ATTEMPTS_SHORT = 10
    private const val WAIT_ATTEMPTS_LONG = 20

    @Volatile private var lastEmptyMessageTime: Long = 0L
    @Volatile private var eventsRegistered: Boolean = false

    private fun ensureEventsRegistered() {
        if (!eventsRegistered) {
            Events.register(this)
            eventsRegistered = true
        }
    }

    fun unregisterEvents() {
        if (eventsRegistered) {
            Events.unregister(this)
            eventsRegistered = false
        }
    }

    @Subscribe
    fun onMessage(event: MessageEvent) {
        if (event.message == EMPTY_MESSAGE) {
            lastEmptyMessageTime = System.currentTimeMillis()
        }
    }

    // --- Detection helpers ---

    fun inventoryPouch(): Item? {
        val pouch = Inventory.stream().id(RUNE_POUCH, DIVINE_RUNE_POUCH).first()
        return if (pouch.valid()) pouch else null
    }

    fun bankPouch(): Item? {
        if (!Bank.opened()) return null
        val pouch = Bank.stream().id(RUNE_POUCH, DIVINE_RUNE_POUCH).first()
        return if (pouch.valid()) pouch else null
    }

    fun hasPouchInInventory(): Boolean = inventoryPouch() != null
    fun hasPouchInBank(): Boolean = bankPouch() != null

    fun currentType(): Type? {
        val pouch = inventoryPouch() ?: return null
        return when (pouch.id()) {
            RUNE_POUCH -> Type.NORMAL
            DIVINE_RUNE_POUCH -> Type.DIVINE
            else -> null
        }
    }

    // --- Ensure pouch in inventory ---

    fun ensurePouchInInventory(): Boolean {
        if (hasPouchInInventory()) return true
        if (!Bank.opened()) return false

        val withdrew = Bank.withdraw(RUNE_POUCH, 1) || Bank.withdraw(DIVINE_RUNE_POUCH, 1)
        if (!withdrew) return false

        return Condition.wait({ hasPouchInInventory() }, WAIT_INTERVAL_SHORT, WAIT_ATTEMPTS_LONG)
    }

    // --- Emptying ---

    // Empties pouch repeatedly until "The pouch is empty." message
    fun emptyCompletely(maxAttempts: Int = 3): Boolean {
        ensureEventsRegistered()

        repeat(maxAttempts) {
            val pouch = inventoryPouch() ?: return false
            val before = lastEmptyMessageTime

            if (!pouch.interact("Empty")) return false

            val sawEmptyMsg = Condition.wait(
                { lastEmptyMessageTime > before },
                WAIT_INTERVAL_LONG,
                WAIT_ATTEMPTS_SHORT
            )
            if (sawEmptyMsg) return true
        }
        return false
    }

    fun emptyAtBank(): Boolean {
        if (!Bank.opened()) return false
        if (!ensurePouchInInventory()) return false
        return emptyCompletely()
    }

    // --- Filling ---

    // Fills pouch with runes already in inventory
    fun fillFromInventory(runeIds: List<Int>): FillResult? {
        if (runeIds.isEmpty()) return null
        if (!ensurePouchInInventory()) return null

        val type = currentType() ?: return null
        if (runeIds.size > type.maxRunes) return null

        val loaded = mutableMapOf<Int, Int>()
        runeIds.forEach { runeId ->
            val moved = depositRuneStackIntoPouch(runeId)
            if (moved > 0) loaded[runeId] = moved
        }
        return FillResult(type, loaded)
    }

    // Full setup: clears pouch, withdraws runes from bank, fills pouch
    fun clearAndFillFromBankWithResult(
        runeIds: List<Int>,
        amountPerRune: Int = CAPACITY_PER_RUNE,
        clearBeforeFill: Boolean = true
    ): FillResult? {
        if (runeIds.isEmpty()) return null
        if (!Bank.opened()) return null
        if (!ensurePouchInInventory()) return null

        val type = currentType() ?: return null
        if (runeIds.size > type.maxRunes) return null

        if (clearBeforeFill && !emptyCompletely()) return null

        runeIds.forEach { runeId -> withdrawRuneStack(runeId, amountPerRune) }

        // Close bank to use runes on pouch
        if (Bank.opened()) {
            if (!Bank.close()) {
                Condition.wait({ !Bank.opened() }, WAIT_INTERVAL_SHORT, WAIT_ATTEMPTS_LONG)
            } else {
                Condition.wait({ !Bank.opened() }, WAIT_INTERVAL_SHORT, WAIT_ATTEMPTS_SHORT)
            }
        }

        return fillFromInventory(runeIds)
    }

    fun clearAndFillFromBank(
        runeIds: List<Int>,
        amountPerRune: Int = CAPACITY_PER_RUNE,
        clearBeforeFill: Boolean = true
    ): Boolean = clearAndFillFromBankWithResult(runeIds, amountPerRune, clearBeforeFill) != null

    // --- Low-level helpers ---

    private fun withdrawRuneStack(runeId: Int, amount: Int): Boolean {
        if (!Bank.opened()) return false
        val success = Bank.withdraw(runeId, amount)
        if (!success) return false
        return Condition.wait(
            { Inventory.stream().id(runeId).first().valid() },
            WAIT_INTERVAL_SHORT,
            WAIT_ATTEMPTS_LONG
        )
    }

    private fun ensureNoItemSelected() {
        val selected = Inventory.selectedItem()
        if (selected.id() != -1) {
            selected.interact("Use")
            Condition.wait({ Inventory.selectedItem().id() == -1 }, WAIT_INTERVAL_SHORT, WAIT_ATTEMPTS_SHORT)
        }
    }

    // Uses "Use rune -> Use pouch" to deposit a rune stack
    private fun depositRuneStackIntoPouch(runeId: Int): Int {
        val pouch = inventoryPouch() ?: return 0
        val runeItem = Inventory.stream().id(runeId).first()
        if (!runeItem.valid()) return 0

        val before = runeItem.stackSize()

        if (!Game.tab(Tab.INVENTORY)) return 0
        ensureNoItemSelected()

        if (!runeItem.interact("Use")) return 0

        val selected = Condition.wait(
            { Inventory.selectedItem().id() == runeItem.id() },
            WAIT_INTERVAL_SHORT,
            WAIT_ATTEMPTS_SHORT
        )
        if (!selected) return 0

        if (!pouch.interact("Use")) return 0

        Condition.wait({
            val current = Inventory.stream().id(runeId).first()
            !current.valid() || current.stackSize() != before
        }, WAIT_INTERVAL_LONG, WAIT_ATTEMPTS_LONG)

        val after = Inventory.stream().id(runeId).first().let { item ->
            if (item.valid()) item.stackSize() else 0
        }

        val moved = (before - after).coerceAtLeast(0)
        ensureNoItemSelected()
        return moved
    }
}