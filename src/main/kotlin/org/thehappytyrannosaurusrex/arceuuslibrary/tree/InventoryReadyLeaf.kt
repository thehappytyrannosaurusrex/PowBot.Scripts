/* Project: Arceuus Library Script (PowBot) */

package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.dax.api.models.RunescapeBank
import org.powbot.dax.api.DaxWalker
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.Logger
import kotlin.random.Random
import org.thehappytyrannosaurusrex.api.utils.ScriptUtils


class InventoryReadyLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Setup & travel") {

    companion object {

        private val GRACEFUL_PIECES = listOf(
            "Graceful hood", "Graceful top", "Graceful legs",
            "Graceful gloves", "Graceful boots", "Graceful cape"
        )

        // Stamina stocking goals
        private const val TARGET_STAMINA_DOSES = 16
        private const val MIN_STARTUP_STAMINA_DOSES = 8
        private const val MAX_STAMINA_ITEMS = 6

        // Stamina names
        private const val STAMINA1_NAME = "Stamina potion(1)"
        private const val STAMINA2_NAME = "Stamina potion(2)"
        private const val STAMINA3_NAME = "Stamina potion(3)"
        private const val STAMINA4_NAME = "Stamina potion(4)"

        private val STAMINA_NAMES_BY_DOSE_ASC = listOf(
            STAMINA1_NAME, STAMINA2_NAME, STAMINA3_NAME, STAMINA4_NAME
        )

    }

    private var loggedReadyInsideLibrary = false
    private var initialStaminaCheckDone = false

        // -------- Banking travel --------
        private fun moveToBank() {
            val local = Players.local()

            if (!local.valid()) {
                Logger.info("[Arceuus Library] LOGIC | Local player invalid, using generic moveToBank().")
                Movement.moveToBank()
                return
            }

            val distToArceuus = local.tile().distanceTo(Locations.ARCEUUS_BANK_TILE)
            if (distToArceuus < 50.0) {
                Logger.info(
                    "[Arceuus Library] LOGIC | ~${"%.1f".format(distToArceuus)} tiles to Arceuus bank; " +
                            "try moveToBank(ARCEUUS)."
                )
                Movement.moveToBank(RunescapeBank.ARCEUUS)
                if (!Bank.present()) {
                    Logger.info(
                        "[Arceuus Library] LOGIC | Arceuus bank not present; " +
                                "fallback step(${Locations.ARCEUUS_BANK_TILE})."
                    )
                    Movement.step(Locations.ARCEUUS_BANK_TILE)
                }
            } else {
                Logger.info(
                    "[Arceuus Library] LOGIC | Far from Arceuus bank " +
                            "(dist=${"%.1f".format(distToArceuus)}); generic moveToBank()."
                )
                Movement.moveToBank()
            }
        }


        // -------- Error helper --------
private fun stopWithError(message: String): Boolean {
    ScriptUtils.stopWithError(script, message)
    return false
}

// -------- Graceful handling --------

    private fun hasFullGracefulEquipped(): Boolean =
        GRACEFUL_PIECES.all { pieceName ->
            Equipment.stream().name(pieceName).first().valid()
        }

    private fun equipGracefulFromInventory(): Boolean {
        if (Bank.opened()) return false

        val missingPieceToEquip = GRACEFUL_PIECES.firstOrNull { pieceName ->
            val equipped = Equipment.stream().name(pieceName).first().valid()
            val inInv = Inventory.stream().name(pieceName).first().valid()
            !equipped && inInv
        } ?: return false

        val item = Inventory.stream().name(missingPieceToEquip).first()
        if (!item.valid()) return false

        Logger.info("[Arceuus Library] LOGIC | Equipping ${item.name()}.")
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
                val bItem = Bank.stream().name(piece).first()
                if (!bItem.valid()) {
                    return stopWithError("[Setup:Graceful] Missing '$piece' in bank; cannot equip full set. Stopping.")
                }
                Logger.info("[Arceuus Library] LOGIC | Withdrawing $piece from bank.")
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

    // -------- Stamina handling --------
    private fun itemStaminaDoses(name: String): Int = when (name) {
        STAMINA1_NAME -> 1; STAMINA2_NAME -> 2; STAMINA3_NAME -> 3; STAMINA4_NAME -> 4
        else -> 0
    }

    private fun currentStaminaDoses(): Int {
        var total = 0
        Inventory.stream().forEach { item -> total += itemStaminaDoses(item.name()) }
        return total
    }

    private fun currentStaminaItemCount(): Int {
        var count = 0
        Inventory.stream().forEach { item -> if (itemStaminaDoses(item.name()) > 0) count++ }
        return count
    }

    private fun bankStaminaDosesAll(): Int {
        var total = 0
        Bank.stream().forEach { item -> total += itemStaminaDoses(item.name()) }
        return total
    }

    fun ensureStamina(): Boolean {
        if (!script.shouldUseStamina()) return true

        val current = currentStaminaDoses()
        val currentItems = currentStaminaItemCount()

        if (!initialStaminaCheckDone) {
            initialStaminaCheckDone = true
            if (current >= MIN_STARTUP_STAMINA_DOSES) {
                Logger.info("[Arceuus Library] LOGIC | Startup: already have $current doses (>= $MIN_STARTUP_STAMINA_DOSES); skipping bank.")
                return true
            }
        }

        if (!Bank.present()) { Logger.info("[Arceuus Library] LOGIC | Need stamina setup; heading to bank."); moveToBank(); return false }
        if (!Bank.opened()) { Logger.info("[Arceuus Library] LOGIC | Opening bank for stamina."); Bank.open(); return false }

        val bankDoses = bankStaminaDosesAll()
        val totalPotentialDoses = current + bankDoses
        if (totalPotentialDoses <= 0) {
            return stopWithError("[Setup:Stamina] No stamina potions in inventory or bank. Stopping.")
        }

        val desiredDoses = minOf(TARGET_STAMINA_DOSES, totalPotentialDoses)
        if (current >= desiredDoses) return true

        val maxNewItems = (MAX_STAMINA_ITEMS - currentItems).coerceAtLeast(0)
        if (maxNewItems == 0) {
            Logger.info("[Arceuus Library] LOGIC | Already have $currentItems stamina items (max $MAX_STAMINA_ITEMS).")
            return true
        }

        val remainingDosesNeeded = desiredDoses - current
        val toWithdrawName = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull { name ->
            val doses = itemStaminaDoses(name)
            val itemInBank = Bank.stream().name(name).first()
            itemInBank.valid() && remainingDosesNeeded > 0 && doses > 0
        }

        if (toWithdrawName == null) {
            Logger.info("[Arceuus Library] LOGIC | No more stamina in bank; using $current/$desiredDoses doses.")
            return true
        }

        Logger.info("[Arceuus Library] LOGIC | Need ~$remainingDosesNeeded more doses; withdrawing 1 x $toWithdrawName.")
        Bank.withdraw(toWithdrawName, 1)
        return false
    }

    fun maybeUseStaminaAndRestock(): Boolean {
        if (!script.shouldUseStamina()) return false
        val energy = Movement.energyLevel()
        val doses = currentStaminaDoses()

        // Drink when less than 30 run energy
        if (energy < 30 && doses > 0) {
            val smallest = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull { n -> Inventory.stream().name(n).first().valid() }
            if (smallest != null) {
                Logger.info("[Arceuus Library] LOGIC | Energy=$energy%, drinking $smallest.")
                Inventory.stream().name(smallest).first().interact("Drink")
                return true
            }
        }

        // Restock when out of doses & low energy
        if (doses == 0 && energy < 10) {
            Logger.info("[Arceuus Library] LOGIC | 0 doses and energy=$energy%; restocking.")
            moveToBank()
            return true
        }
        return false
    }

    // -------- Travel to Arceuus Library --------

    private fun travelToLibrary(): Boolean {
        val local = Players.local()
        if (!local.valid()) return false
        val here = local.tile()

        // Only consider travel "finished" when 're near the anchor library tile.
        if (here.floor == Locations.LIBRARY_TILE.floor &&
            here.distanceTo(Locations.LIBRARY_TILE) <= 3.0
        ) {
            if (!loggedReadyInsideLibrary) {
                Logger.info(
                    "[Arceuus Library] LOGIC | Arrived at ground-floor library anchor " +
                            "${Locations.LIBRARY_TILE}."
                )
                loggedReadyInsideLibrary = true
            }
            return true
        }
        loggedReadyInsideLibrary = false

        val dist = here.distanceTo(Locations.LIBRARY_TILE)
        Logger.info(
            "[Arceuus Library] LOGIC | Close to Library " +
                    "(dist=${"%.1f".format(dist)}); walkTo(${Locations.LIBRARY_TILE})."
        )
        Movement.moveTo(Locations.LIBRARY_TILE)

        return false
    }


// -------- Main tick --------
    override fun execute() {

        // Graceful & stamina setup first
        val gracefulOk = ensureGraceful()
        val staminaOk = ensureStamina()
        if (!gracefulOk || !staminaOk) return

        // Runtime stamina policy
        if (maybeUseStaminaAndRestock()) return

        // Travel
        travelToLibrary()
    }
}