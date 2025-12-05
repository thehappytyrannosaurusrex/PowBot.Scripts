package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players

object BankUtils {

    // =========================================================================
    // Deposit Operations
    // =========================================================================

    fun depositInventoryToNearestBank(
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[Bank]"
    ): Boolean {
        return depositToNearestBankInternal(preferredBankTile, maxPreferredDistance, logPrefix) {
            if (Inventory.isNotEmpty()) {
                Logger.info("$logPrefix Depositing full inventory.")
                Bank.depositInventory()
            } else {
                Logger.info("$logPrefix Inventory already empty.")
            }
        }
    }

    // Deposit all except specified IDs (primary method - matches PowBot API)
    fun depositAllExceptToNearestBank(
        keepItemIds: IntArray,
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[Bank]"
    ): Boolean {
        return depositToNearestBankInternal(preferredBankTile, maxPreferredDistance, logPrefix) {
            if (Inventory.isNotEmpty()) {
                Logger.info("$logPrefix Depositing all except ${keepItemIds.toList()}")
                Bank.depositAllExcept(*keepItemIds)
            } else {
                Logger.info("$logPrefix Inventory already empty.")
            }
        }
    }

    // Deposit all except specified names (String-based convenience)
    fun depositAllExceptByName(
        keepItemNames: Set<String>,
        preferredBankTile: Tile? = null,
        maxPreferredDistance: Double = 200.0,
        logPrefix: String = "[Bank]"
    ): Boolean {
        return depositToNearestBankInternal(preferredBankTile, maxPreferredDistance, logPrefix) {
            if (Inventory.isNotEmpty()) {
                val normalizedKeep = keepItemNames.map { it.lowercase().trim() }.toSet()
                Logger.info("$logPrefix Depositing all except: $keepItemNames")

                Inventory.stream()
                    .filter { it.valid() && it.name().lowercase().trim() !in normalizedKeep }
                    .forEach { item ->
                        // Use item ID with Bank.Amount enum - correct PowBot API signature
                        Bank.deposit(item.id(), Bank.Amount.ALL)
                    }
            } else {
                Logger.info("$logPrefix Inventory already empty.")
            }
        }
    }

    // =========================================================================
    // Withdraw Operations
    // =========================================================================

    // Withdraw item by ID
    fun withdrawItemById(id: Int, amount: Int = 1): Boolean {
        if (!Bank.opened()) return false
        return Bank.withdraw(id, amount)
    }

    // Withdraw item by name
    fun withdrawItem(name: String, amount: Int = 1): Boolean {
        if (!Bank.opened()) return false
        val item = findBankItem(name)
        if (!item.valid()) return false
        return Bank.withdraw(item.id(), amount)
    }

    // =========================================================================
    // Bank Item Checks
    // =========================================================================

    fun hasBankItem(name: String): Boolean = Bank.stream().name(name).isNotEmpty()
    fun hasBankItemById(id: Int): Boolean = Bank.stream().id(id).isNotEmpty()
    fun findBankItem(name: String): Item = Bank.stream().name(name).first()

    // =========================================================================
    // Deposit Single Item
    // =========================================================================

    fun depositInvItem(name: String, amount: Int = -1): Boolean {
        if (!Bank.opened()) return false
        val item = Inventory.stream().name(name).first()
        if (!item.valid()) return false
        // Use item ID with Bank.Amount or int - correct PowBot API signature
        return if (amount == -1) {
            Bank.deposit(item.id(), Bank.Amount.ALL)
        } else {
            Bank.deposit(item.id(), amount)
        }
    }

    fun depositInvItemById(id: Int, amount: Int = -1): Boolean {
        if (!Bank.opened()) return false
        val item = Inventory.stream().id(id).first()
        if (!item.valid()) return false
        return if (amount == -1) {
            Bank.deposit(item.id(), Bank.Amount.ALL)
        } else {
            Bank.deposit(item.id(), amount)
        }
    }

    // =========================================================================
    // Internal: handles walking to bank and opening
    // =========================================================================

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
            if (dist <= maxPreferredDistance) {
                Logger.info("$logPrefix Walking to preferred bank tile $preferredBankTile (dist=${"%.1f".format(dist)}).")
                Movement.moveTo(preferredBankTile)
                return false
            }
        }

        Logger.info("$logPrefix No preferred bank tile nearby; using Movement.moveToBank().")
        Movement.moveToBank()
        return false
    }
}