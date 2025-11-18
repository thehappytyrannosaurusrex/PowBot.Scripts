package org.thehappytyrannosaurusrex.api.bank

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.api.utils.Logger

/**
 * Shared helpers for interacting with banks.
 */
object BankUtils {

    /**
     * Walks to the nearest bank (optionally preferring a specific tile) and deposits
     * the entire inventory.
     *
     * Call this from a Leaf's execute; it is safe to call repeatedly.
     *
     * @return true if banking is finished (inventory empty or deposited), false otherwise.
     */
    fun depositInventoryToNearestBank(
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[BankUtils]"
    ): Boolean {

        if (Inventory.isEmpty()) {
            Logger.info("$logPrefix Inventory empty; nothing to deposit.")
            return true
        }

        // If we can see a bank, open it and dump inventory.
        if (Bank.present()) {
            if (!Bank.opened()) {
                Logger.info("$logPrefix Bank nearby; opening.")
                Bank.open()
                return false
            }

            if (Inventory.isNotEmpty()) {
                Logger.info("$logPrefix Depositing full inventory.")
                Bank.depositInventory()
            } else {
                Logger.info("$logPrefix Inventory already empty; nothing to deposit.")
            }
            return true
        }

        val local = Players.local()
        if (preferredBankTile != null && local.valid()) {
            val dist = local.tile().distanceTo(preferredBankTile)
            if (dist <= maxPreferredDistance) {
                Logger.info(
                    "$logPrefix Walking to preferred bank tile $preferredBankTile " +
                            "(dist=${"%.1f".format(dist)})."
                )
                Movement.moveTo(preferredBankTile)
                return false
            }
        }

        Logger.info("$logPrefix No preferred bank in range; using Movement.moveToBank().")
        Movement.moveToBank()
        return false
    }
}
