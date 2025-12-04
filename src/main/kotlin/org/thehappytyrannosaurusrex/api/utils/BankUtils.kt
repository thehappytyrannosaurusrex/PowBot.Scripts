package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players

object BankUtils {

    fun depositInventoryToNearestBank(
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[Bank]"
    ): Boolean {
        return depositToNearestBankInternal(
            preferredBankTile = preferredBankTile,
            maxPreferredDistance = maxPreferredDistance,
            logPrefix = logPrefix
        ) {
            if (Inventory.isNotEmpty()) {
                Logger.info("$logPrefix Depositing full inventory.")
                Bank.depositInventory()
            } else {
                Logger.info("$logPrefix Inventory already empty; nothing to deposit.")
            }
        }
    }

    /**
 * Walks to the nearest bank and deposits everything except [keepItemIds].
 */
    fun depositAllExceptToNearestBank(
        keepItemIds: IntArray,
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[Bank]"
    ): Boolean {
        return depositToNearestBankInternal(
            preferredBankTile = preferredBankTile,
            maxPreferredDistance = maxPreferredDistance,
            logPrefix = logPrefix
        ) {
            if (Inventory.isNotEmpty()) {
                Logger.info(
                    "$logPrefix Depositing all except " +
                            keepItemIds.joinToString(prefix = "[", postfix = "]")
                )
                Bank.depositAllExcept(*keepItemIds)
            } else {
                Logger.info("$logPrefix Inventory already empty; nothing to deposit.")
            }
        }
    }

    /**
 * Shared guts: get to a bank + open it, then run [depositAction].
 */
    private fun depositToNearestBankInternal(
        preferredBankTile: Tile?,
        maxPreferredDistance: Double, // kept for backwards compat; no longer used
        logPrefix: String,
        depositAction: () -> Unit
    ): Boolean {

        if (Inventory.isEmpty()) {
            Logger.info("$logPrefix Inventory empty; nothing to deposit.")
            return true
        }

        // If can see a bank, open it and dump inventory.
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
            Logger.info(
                "$logPrefix Walking to preferred bank tile $preferredBankTile " +
                        "(dist=${"%.1f".format(dist)})."
            )
            Movement.moveTo(preferredBankTile)
            return false
        }

        Logger.info("$logPrefix No preferred bank tile specified; using Movement.moveToBank().")
        Movement.moveToBank()
        return false
    }

}