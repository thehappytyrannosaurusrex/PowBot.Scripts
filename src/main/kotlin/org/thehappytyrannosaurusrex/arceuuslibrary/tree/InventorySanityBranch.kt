package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils
import org.thehappytyrannosaurusrex.api.utils.Logger

class InventorySanityBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Inventory sanity check") {

    companion object {
        private val GRACEFUL_NAMES = setOf(
            "Graceful hood", "Graceful top", "Graceful legs",
            "Graceful gloves", "Graceful boots", "Graceful cape"
        )

        private val STAMINA_PREFIXES = listOf("Stamina potion(")

        private val TRAVEL_NAME_SNIPPETS = setOf(
            "teleport", "xeric's talisman", "dramen staff"
        )

        private val TRAVEL_RUNES = setOf(
            "Law rune", "Air rune", "Earth rune", "Water rune", "Fire rune",
            "Dust rune", "Mist rune", "Mud rune", "Steam rune", "Smoke rune"
        )
    }

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()
    private val depositLeaf = DepositInventoryAtBankLeaf(script)
    private val travelOrLibraryBranch = TravelOrLibraryBranch(script)

    private fun isStaminaPotion(name: String): Boolean =
        STAMINA_PREFIXES.any { name.startsWith(it, ignoreCase = true) }

    private fun isGraceful(name: String): Boolean = name in GRACEFUL_NAMES

    private fun isTravelAllowed(name: String): Boolean {
        if (!script.shouldAllowTravelItems()) return false
        val lower = name.lowercase()
        return TRAVEL_NAME_SNIPPETS.any { lower.contains(it) } || name in TRAVEL_RUNES
    }

    override fun validate(): Boolean {
        val hasNonAllowedItems = InventoryUtils.ById.hasNonAllowedItems(
            allowedItemIds = bookItemIds,
            allowedByName = listOf(::isGraceful, ::isStaminaPotion, ::isTravelAllowed)
        )

        if (hasNonAllowedItems) {
            Logger.info("[Arceuus Library] LOGIC | Found non-allowed items; will bank.")
        }
        return hasNonAllowedItems
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = depositLeaf

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = travelOrLibraryBranch
}