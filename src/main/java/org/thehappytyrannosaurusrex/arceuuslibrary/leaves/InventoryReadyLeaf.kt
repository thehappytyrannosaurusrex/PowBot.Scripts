/*
 * Project: Arceuus Library Script (PowBot)
 * File: InventoryReadyLeaf.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.leaves

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.dax.api.models.RunescapeBank
import org.powbot.dax.api.DaxWalker
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/** Inventory clean -> handle Graceful + stamina setup -> travel to Arceuus Library. */
class InventoryReadyLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Setup & travel") {

    companion object {
        private val ARCEUUS_BANK_TILE = Tile(1629, 3746, 0)
        private val LIBRARY_TILE = Tile(1632, 3804, 0)

        private val GRACEFUL_PIECES = listOf(
            "Graceful hood", "Graceful top", "Graceful legs",
            "Graceful gloves", "Graceful boots", "Graceful cape"
        )

        // stamina stocking goals
        private const val TARGET_STAMINA_DOSES = 16
        private const val MIN_STARTUP_STAMINA_DOSES = 8
        private const val MAX_STAMINA_ITEMS = 6

        // stamina names
        private const val STAMINA1_NAME = "Stamina potion(1)"
        private const val STAMINA2_NAME = "Stamina potion(2)"
        private const val STAMINA3_NAME = "Stamina potion(3)"
        private const val STAMINA4_NAME = "Stamina potion(4)"

        private val STAMINA_NAMES_BY_DOSE_ASC = listOf(
            STAMINA1_NAME, STAMINA2_NAME, STAMINA3_NAME, STAMINA4_NAME
        )

        // movement-based failsafe (short & strict)
        private const val NO_PROGRESS_WINDOW_MS = 6_000L
        private const val REQUIRED_PROGRESS_TILES = 3.0          // must shrink distance by >= 3 tiles
        private const val MAX_NO_PROGRESS_TRIES = 5              // before Dax
        private const val MAX_NO_PROGRESS_AFTER_DAX = 3          // then stop
    }

    private var loggedReadyInsideLibrary = false
    private var initialStaminaCheckDone = false
    private var kickedOffLibraryFlow = false

    // ---- movement watchdog (library) ----
    private var lastProgressAt: Long = 0L
    private var lastDistance: Double? = null
    private var noProgressTries: Int = 0
    private var daxUsedOnce: Boolean = false

    // -------- Banking travel --------
    private fun moveToBank() {
        val local = Players.local()
        if (!local.valid()) {
            Logger.info("[Travel] Local player invalid, using generic moveToBank().")
            Movement.moveToBank()
            return
        }

        val distToArceuus = local.tile().distanceTo(ARCEUUS_BANK_TILE)
        if (distToArceuus < 50.0) {
            Logger.info("[Travel] ~${"%.1f".format(distToArceuus)} tiles to Arceuus bank; try moveToBank(ARCEUUS).")
            Movement.moveToBank(RunescapeBank.ARCEUUS)
            if (!Bank.present()) {
                Logger.info("[Travel] Arceuus bank not present; fallback step($ARCEUUS_BANK_TILE).")
                Movement.step(ARCEUUS_BANK_TILE)
            }
        } else {
            Logger.info("[Travel] Far from Arceuus (dist=${"%.1f".format(distToArceuus)}); generic moveToBank().")
            Movement.moveToBank()
        }
    }

    // -------- Error helper --------
    private fun stopWithError(message: String): Boolean {
        Logger.error(message)
        script.controller.stop()
        return true
    }

    // -------- Graceful handling --------
    /** Public integrity check: all six Graceful slots by name. */
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

        Logger.info("[Setup:Graceful] Equipping ${item.name()}.")
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
                Logger.info("[Setup:Graceful] Withdrawing $piece from bank.")
                Bank.withdraw(piece, 1)
                withdrewAny = true
            }
        }
        if (withdrewAny) Bank.close()
        return !withdrewAny
    }

    private fun ensureGraceful(): Boolean {
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

    /** Stock/withdraw stamina per policy. */
    private fun ensureStamina(): Boolean {
        if (!script.shouldUseStamina()) return true

        val current = currentStaminaDoses()
        val currentItems = currentStaminaItemCount()

        if (!initialStaminaCheckDone) {
            initialStaminaCheckDone = true
            if (current >= MIN_STARTUP_STAMINA_DOSES) {
                Logger.info("[Setup:Stamina] Startup: already have $current doses (>= $MIN_STARTUP_STAMINA_DOSES); skipping bank.")
                return true
            }
        }

        if (!Bank.present()) { Logger.info("[Setup:Stamina] Need stamina setup; heading to bank."); moveToBank(); return false }
        if (!Bank.opened()) { Logger.info("[Setup:Stamina] Opening bank for stamina."); Bank.open(); return false }

        val bankDoses = bankStaminaDosesAll()
        val totalPotentialDoses = current + bankDoses
        if (totalPotentialDoses <= 0) {
            return stopWithError("[Setup:Stamina] No stamina potions in inventory or bank. Stopping.")
        }

        val desiredDoses = minOf(TARGET_STAMINA_DOSES, totalPotentialDoses)
        if (current >= desiredDoses) return true

        val maxNewItems = (MAX_STAMINA_ITEMS - currentItems).coerceAtLeast(0)
        if (maxNewItems == 0) {
            Logger.info("[Setup:Stamina] Already have $currentItems stamina items (max $MAX_STAMINA_ITEMS).")
            return true
        }

        val remainingDosesNeeded = desiredDoses - current
        val toWithdrawName = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull { name ->
            val doses = itemStaminaDoses(name)
            val itemInBank = Bank.stream().name(name).first()
            itemInBank.valid() && remainingDosesNeeded > 0 && doses > 0
        }

        if (toWithdrawName == null) {
            Logger.info("[Setup:Stamina] No more stamina in bank; using $current/$desiredDoses doses.")
            return true
        }

        Logger.info("[Setup:Stamina] Need ~$remainingDosesNeeded more doses; withdrawing 1 x $toWithdrawName.")
        Bank.withdraw(toWithdrawName, 1)
        return false
    }

    /** Runtime stamina usage + restock trigger (not just initial setup). */
    private fun maybeUseStaminaAndRestock(): Boolean {
        if (!script.shouldUseStamina()) return false
        val energy = Movement.energyLevel()
        val doses = currentStaminaDoses()

        // Drink when less than 30 run energy
        if (energy < 30 && doses > 0) {
            val smallest = STAMINA_NAMES_BY_DOSE_ASC.firstOrNull { n -> Inventory.stream().name(n).first().valid() }
            if (smallest != null) {
                Logger.info("[Stamina] Energy=$energy%, drinking $smallest.")
                Inventory.stream().name(smallest).first().interact("Drink")
                return true
            }
        }

        // Restock when out of doses & low energy
        if (doses == 0 && energy < 10) {
            Logger.info("[Stamina] 0 doses and energy=$energy%; restocking.")
            moveToBank()
            return true
        }
        return false
    }

    // -------- Travel to Arceuus Library (distance-progress failsafe) --------
    private fun updateProgressAndMaybeFail(distNow: Double) {
        val now = System.currentTimeMillis()
        val prev = lastDistance

        if (prev == null) {
            lastDistance = distNow
            lastProgressAt = now
            noProgressTries = 0
            return
        }

        val shrink = prev - distNow
        val windowElapsed = now - lastProgressAt >= NO_PROGRESS_WINDOW_MS

        if (shrink >= REQUIRED_PROGRESS_TILES) {
            // made meaningful progress
            lastDistance = distNow
            lastProgressAt = now
            if (noProgressTries > 0) {
                Logger.info("[Travel:Progress] Distance decreased by ${"%.1f".format(shrink)} tiles (now=${"%.1f".format(distNow)}). Resetting attempts.")
            }
            noProgressTries = 0
            return
        }

        if (windowElapsed) {
            noProgressTries++
            Logger.info(
                "[Failsafe] Library no-progress attempt $noProgressTries/${if (!daxUsedOnce) MAX_NO_PROGRESS_TRIES else MAX_NO_PROGRESS_AFTER_DAX} " +
                        "(dist=${"%.1f".format(prev)}→${"%.1f".format(distNow)} in ~${NO_PROGRESS_WINDOW_MS/1000}s; need ≥${"%.0f".format(REQUIRED_PROGRESS_TILES)})."
            )
            lastDistance = distNow
            lastProgressAt = now

            if (!daxUsedOnce && noProgressTries >= MAX_NO_PROGRESS_TRIES) {
                daxUsedOnce = true
                noProgressTries = 0
                Logger.info("[Travel:FailSafe] Max no-progress windows hit; trying DaxWalker.walkTo($LIBRARY_TILE).")
                val ok = try { DaxWalker.walkTo(LIBRARY_TILE) } catch (_: Exception) { false }
                if (!ok) {
                    stopWithError("[Travel:FailSafe] DaxWalker failed to start path to Library. Stopping.")
                }
            } else if (daxUsedOnce && noProgressTries >= MAX_NO_PROGRESS_AFTER_DAX) {
                stopWithError("[Travel:FailSafe] Still no distance progress after Dax escalation. Stopping.")
            }
        }
    }

    private fun travelToLibrary(): Boolean {
        val local = Players.local()
        if (!local.valid()) return false

        val here = local.tile()

        // InventoryReadyLeaf.kt — inside travelToLibrary(), replace the "inside library" block with:
        if (Locations.isInsideLibrary(here)) {
            // Rate-limit: log once per world+session even if the leaf is re-instantiated
            if (!loggedReadyInsideLibrary) {
                Logger.info("[Travel] Arrived at Arceuus Library.")
                loggedReadyInsideLibrary = true
            }
            // reset watchdog on arrival
            lastDistance = 0.0
            lastProgressAt = System.currentTimeMillis()
            noProgressTries = 0
            daxUsedOnce = false
            return true
        }
        loggedReadyInsideLibrary = false

        // Calculate distance and update failsafe BEFORE we issue new steps (so taps don't mask being stuck)
        val dist = here.distanceTo(LIBRARY_TILE)
        updateProgressAndMaybeFail(dist)

        // Keep issuing a step/walk towards the goal (Movement handles projecting off-map targets internally)
        if (dist <= 25.0) {
            Logger.info("[Travel] Close to Library (dist=${"%.1f".format(dist)}); walkTo($LIBRARY_TILE).")
            Movement.walkTo(LIBRARY_TILE)
        } else {
            Logger.info("[Travel] Heading to Library from $here (dist=${"%.1f".format(dist)}); step($LIBRARY_TILE).")
            Movement.step(LIBRARY_TILE)
        }

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