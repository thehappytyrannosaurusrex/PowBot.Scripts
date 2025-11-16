/*
 * Project: Arceuus Library Script (PowBot)
 * File: DepositInventoryAtBankLeaf.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.leaves

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * DepositInventoryAtBankLeaf: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class DepositInventoryAtBankLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Deposit inventory at bank") {

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()

    companion object {
        private val ARCEUUS_BANK_TILE = Tile(1629, 3746, 0)
        private const val ARCEUUS_MAX_DISTANCE = 200.0

        private const val NO_PROGRESS_WINDOW_MS = 6_000L
        private const val REQUIRED_PROGRESS_TILES = 3.0
        private const val MAX_NO_PROGRESS_TRIES = 5
    }

    private var lastProgressAt = 0L
    private var lastDistance: Double? = null
    private var noProgressTries = 0

    private fun hasNonBookItems(): Boolean =
        Inventory.stream().anyMatch { item ->
            val id = item.id()
            id != -1 && id !in bookItemIds
        }

    private fun distanceToArceuusBank(): Double {
        val local = Players.local()
        if (!local.valid()) return Double.MAX_VALUE
        return local.tile().distanceTo(ARCEUUS_BANK_TILE)
    }

    private fun updateProgressAndMaybeFail(distNow: Double) {
        val now = System.currentTimeMillis()
        val prev = lastDistance
        if (prev == null) {
            lastDistance = distNow
            lastProgressAt = now
            return
        }
        val shrink = prev - distNow
        val windowElapsed = now - lastProgressAt >= NO_PROGRESS_WINDOW_MS
        if (shrink >= REQUIRED_PROGRESS_TILES) {
            lastDistance = distNow
            lastProgressAt = now
            if (noProgressTries > 0) {
                Logger.info("[DepositInv] [DepositInv:Progress] Distance decreased by ${"%.1f".format(shrink)} tiles (now=${"%.1f".format(distNow)}). Resetting attempts.")
            }
            noProgressTries = 0
            return
        }
        if (windowElapsed) {
            noProgressTries++
            Logger.info("[DepositInv] [Failsafe] Bank no-progress attempt $noProgressTries/$MAX_NO_PROGRESS_TRIES (dist=${"%.1f".format(prev)}→${"%.1f".format(distNow)} in ~${NO_PROGRESS_WINDOW_MS/1000}s; need ≥${"%.0f".format(REQUIRED_PROGRESS_TILES)}).")
            lastDistance = distNow
            lastProgressAt = now
            if (noProgressTries >= MAX_NO_PROGRESS_TRIES) {
                stopWithError("[Failsafe] Could not make progress toward bank after $MAX_NO_PROGRESS_TRIES windows. Stopping.")
            }
        }
    }

    private fun stopWithError(message: String) {
        Logger.error(message)
        script.controller.stop()
    }

    override fun execute() {
        if (!hasNonBookItems()) {
            Logger.info("[DepositInv] [DepositInv] No non-book items left; skipping bank.")
            noProgressTries = 0
            return
        }

        val dist = distanceToArceuusBank()
        updateProgressAndMaybeFail(dist)

        if (Bank.present()) {
            noProgressTries = 0
            if (!Bank.opened()) {
                Logger.info("[DepositInv] [DepositInv] Bank nearby, attempting to open.")
                Bank.open()
                return
            }
            if (Inventory.isNotEmpty()) {
                Logger.info("[DepositInv] [DepositInv] Depositing full inventory.")
                Bank.depositInventory()
            } else {
                Logger.info("[DepositInv] [DepositInv] Inventory already empty; nothing to deposit.")
            }
            return
        }

        if (dist <= ARCEUUS_MAX_DISTANCE) {
            Logger.info("[DepositInv] [DepositInv] Walking to Arceuus bank at $ARCEUUS_BANK_TILE (dist=${"%.1f".format(dist)}).")
            Movement.moveTo(ARCEUUS_BANK_TILE)
        } else {
            Logger.info("[DepositInv] [DepositInv] Arceuus bank too far (dist=${"%.1f".format(dist)} > $ARCEUUS_MAX_DISTANCE); Movement.moveToBank().")
            Movement.moveToBank()
        }
    }
}