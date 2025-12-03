package org.thehappytyrannosaurusrex.api.bank

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.api.utils.Logger

// Shared helpers for bank interactions
object BankUtils {

    // Walks to nearest bank and deposits full inventory
    fun depositInventoryToNearestBank(
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[BankUtils]"
    ): Boolean = depositToNearestBankInternal(preferredBankTile, maxPreferredDistance, logPrefix) {
        if (Inventory.isNotEmpty()) {
            Logger.info("$logPrefix Depositing full inventory.")
            Bank.depositInventory()
        } else {
            Logger.info("$logPrefix Inventory already empty.")
        }
    }

    // Walks to nearest bank and deposits all except specified IDs
    fun depositAllExceptToNearestBank(
        keepItemIds: IntArray,
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[BankUtils]"
    ): Boolean = depositToNearestBankInternal(preferredBankTile, maxPreferredDistance, logPrefix) {
        if (Inventory.isNotEmpty()) {
            Logger.info("$logPrefix Depositing all except ${keepItemIds.joinToString()}")
            Bank.depositAllExcept(*keepItemIds)
        } else {
            Logger.info("$logPrefix Inventory already empty.")
        }
    }

    private fun depositToNearestBankInternal(
        preferredBankTile: Tile?,
        maxPreferredDistance: Double,
        logPrefix: String,
        depositAction: () -> Unit
    ): Boolean {
        if (Inventory.isEmpty()) {
            Logger.info("$logPrefix Inventory empty; nothing to deposit.")
            return true
        }

        if (Bank.present()) {
            if (!Bank.opened()) {
                Logger.info("$logPrefix Bank nearby; opening.")
                Bank.open()
            }
            if (!Bank.opened()) {
                Logger.info("$logPrefix Failed to open bank.")
                return false
            }
            depositAction()
            return true
        }

        val local = Players.local()
        if (preferredBankTile != null && local.valid()) {
            val dist = local.tile().distanceTo(preferredBankTile)
            Logger.info("$logPrefix Walking to preferred bank tile $preferredBankTile (dist=${"%.1f".format(dist)}).")
            Movement.moveTo(preferredBankTile)
            return false
        }

        Logger.info("$logPrefix No preferred bank tile; using Movement.moveToBank().")
        Movement.moveToBank()
        return false
    }
}