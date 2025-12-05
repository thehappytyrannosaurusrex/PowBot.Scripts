package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.dax.api.models.RunescapeBank
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.utils.ScriptUtils

class InventoryReadyLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Setup & travel") {

    companion object {
        private val GRACEFUL_PIECES = listOf(
            "Graceful hood", "Graceful top", "Graceful legs",
            "Graceful gloves", "Graceful boots", "Graceful cape"
        )

        private const val TARGET_STAMINA_DOSES = 16
        private const val MIN_STARTUP_STAMINA_DOSES = 8
        private const val MAX_STAMINA_ITEMS = 6

        private val STAMINA_NAMES_BY_DOSE_ASC = listOf(
            "Stamina potion(1)", "Stamina potion(2)", "Stamina potion(3)", "Stamina potion(4)"
        )
    }

    private var loggedReadyInsideLibrary = false
    private var initialStaminaCheckDone = false

    // --- Graceful handling ---

    private fun hasFullGracefulEquipped(): Boolean =
        GRACEFUL_PIECES.all { Equipment.stream().name(it).first().valid() }

    private fun equipGracefulFromInventory(): Boolean {
        if (Bank.opened()) return false

        val missing = GRACEFUL_PIECES.firstOrNull { piece ->
            !Equipment.stream().name(piece).first().valid() &&
                    Inventory.stream().name(piece).first().valid()
        } ?: return false

        val item = Inventory.stream().name(missing).first()
        Logger.info("[Arceuus Library] GEAR | Equipping $missing.")
        item.interact("Wear")
        return true
    }

    private fun withdrawMissingGracefulPieces(): Boolean {
        if (!Bank.present()) { moveToBank(); return false }
        if (!Bank.opened()) { Bank.open(); return false }

        var withdrewAny = false
        for (piece in GRACEFUL_PIECES) {
            val equipped = Equipment.stream().name(piece).first().valid()
            val inInv = Inventory.stream().name(piece).first().valid()
            if (!equipped && !inInv) {
                val bankItem = Bank.stream().name(piece).first()
                if (!bankItem.valid()) {
                    ScriptUtils.stopWithError(script, "[Arceuus Library] Missing '$piece' in bank.")
                    return false
                }
                Logger.info("[Arceuus Library] GEAR | Withdrawing $piece.")
                Bank.withdraw(piece, 1)
                withdrewAny = true
            }
        }
        if (withdrewAny) Bank.close()
        return !withdrewAny
    }

    fun ensureGraceful(): Boolean {
        if (!script.shouldUseGraceful()) return true
        if (hasFullGracefulEquipped()) return true
        if (equipGracefulFromInventory()) return false
        return withdrawMissingGracefulPieces()
    }

    // --- Stamina handling ---

    private fun itemStaminaDoses(name: String): Int = when (name) {
        "Stamina potion(1)" -> 1
        "Stamina potion(2)" -> 2
        "Stamina potion(3)" -> 3
        "Stamina potion(4)" -> 4
        else -> 0
    }

    private fun currentStaminaDoses(): Int =
        Inventory.stream().sumOf { itemStaminaDoses(it.name()) }

    private fun currentStaminaItemCount(): Int =
        Inventory.stream().count { itemStaminaDoses(it.name()) > 0 }

    private fun bankStaminaDosesAll(): Int =
        Bank.stream().sumOf { itemStaminaDoses(it.name()) }

    fun ensureStamina(): Boolean {
        if (!script.shouldUseStamina()) return true

        val current = currentStaminaDoses()
        val currentItems = currentStaminaItemCount()

        if (!initialStaminaCheckDone) {
            initialStaminaCheckDone = true
            if (current >= MIN_STARTUP_STAMINA_DOSES) {
                Logger.info("[Arceuus Library] STAMINA | Already have $current doses; skipping bank.")
                return true
            }
        }

        if (!Bank.present()) { moveToBank(); return false }
        if (!Bank.opened()) { Bank.open(); return false }

        val bankDoses = bankStaminaDosesAll()
        val totalPotential = current + bankDoses
        if (totalPotential <= 0) {
            ScriptUtils.stopWithError(script, "[Arceuus Library] No stamina potions available.")
            return false
        }

        val desired = minOf(TARGET_STAMINA_DOSES, totalPotential)
        if (current >= desired) return true

        val maxNewItems = (MAX_STAMINA_ITEMS - currentItems).coerceAtLeast(0)
        if (maxNewItems == 0) return true

        val toWithdraw = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull { name ->
            Bank.stream().name(name).first().valid()
        }

        if (toWithdraw != null) {
            Logger.info("[Arceuus Library] STAMINA | Withdrawing $toWithdraw.")
            Bank.withdraw(toWithdraw, 1)
        }
        return false
    }

    fun maybeUseStaminaAndRestock(): Boolean {
        if (!script.shouldUseStamina()) return false

        val energy = Movement.energyLevel()
        val doses = currentStaminaDoses()

        if (energy < 30 && doses > 0) {
            val potion = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull {
                Inventory.stream().name(it).first().valid()
            }
            if (potion != null) {
                Logger.info("[Arceuus Library] STAMINA | Energy=$energy%, drinking $potion.")
                Inventory.stream().name(potion).first().interact("Drink")
                return true
            }
        }

        if (doses == 0 && energy < 10) {
            Logger.info("[Arceuus Library] STAMINA | 0 doses and energy=$energy%; restocking.")
            moveToBank()
            return true
        }
        return false
    }

    // --- Banking / Travel ---

    private fun moveToBank() {
        val local = Players.local()
        if (!local.valid()) {
            Movement.moveToBank()
            return
        }

        val dist = local.tile().distanceTo(Locations.ARCEUUS_BANK_TILE)
        if (dist < 50.0) {
            Logger.info("[Arceuus Library] TRAVEL | Walking to Arceuus bank (dist=${"%.1f".format(dist)}).")
            Movement.moveToBank(RunescapeBank.ARCEUUS)
            if (!Bank.present()) {
                Movement.step(Locations.ARCEUUS_BANK_TILE)
            }
        } else {
            Logger.info("[Arceuus Library] TRAVEL | Far from bank; using generic moveToBank().")
            Movement.moveToBank()
        }
    }

    private fun travelToLibrary(): Boolean {
        val local = Players.local()
        if (!local.valid()) return false

        val here = local.tile()
        if (here.floor == Locations.LIBRARY_TILE.floor && here.distanceTo(Locations.LIBRARY_TILE) <= 3.0) {
            if (!loggedReadyInsideLibrary) {
                Logger.info("[Arceuus Library] TRAVEL | Arrived at library.")
                loggedReadyInsideLibrary = true
            }
            return true
        }
        loggedReadyInsideLibrary = false

        Logger.info("[Arceuus Library] TRAVEL | Walking to library.")
        Movement.moveTo(Locations.LIBRARY_TILE)
        return false
    }

    override fun execute() {
        if (!ensureGraceful()) return
        if (!ensureStamina()) return
        if (maybeUseStaminaAndRestock()) return
        travelToLibrary()
    }
}