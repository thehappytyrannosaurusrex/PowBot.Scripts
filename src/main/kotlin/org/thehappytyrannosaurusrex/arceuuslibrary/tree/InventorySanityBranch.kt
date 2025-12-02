package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.tree.DepositInventoryAtBankLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.tree.InventoryReadyLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.tree.TravelOrLibraryBranch
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.inventory.InventorySanity

class InventorySanityBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Inventory sanity check") {

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()

    private val gracefulNames: Set<String> = setOf(
        "Graceful hood", "Graceful top", "Graceful legs",
        "Graceful gloves", "Graceful boots", "Graceful cape"
    )

    private val staminaPrefixes = listOf("Stamina potion(")

    // Centralised allow-list of travel items (by lowercased name)
    private val travelNameSnippets = setOf(
        "teleport",                      // any tab that contains "teleport"
        "xeric's talisman",
        "dramen staff"
    )

    private val travelRunes = setOf(
        "Law rune", "Air rune", "Earth rune", "Water rune", "Fire rune",
        "Dust rune", "Mist rune", "Mud rune", "Steam rune", "Smoke rune"
    )

    private fun isStaminaPotion(name: String): Boolean =
        staminaPrefixes.any { name.startsWith(it, ignoreCase = true) }

    private fun isGraceful(name: String): Boolean = gracefulNames.contains(name)

    private fun isTravelAllowed(name: String): Boolean {
        val allowTravel = script.shouldAllowTravelItems()
        if (!allowTravel) return false
        val lower = name.lowercase()
        return travelNameSnippets.any { lower.contains(it) } || travelRunes.contains(name)
    }

    private val depositLeaf = DepositInventoryAtBankLeaf(script)
    private val travelOrLibraryBranch = TravelOrLibraryBranch(script)

    override fun validate(): Boolean {
        val hasNonAllowedItems = InventorySanity.hasNonAllowedItems(
            allowedItemIds = bookItemIds, 
            allowedByName = listOf<(String) -> Boolean>(
                ::isGraceful,
                ::isStaminaPotion,
                { name -> isTravelAllowed(name) }
            )
        )

        if (hasNonAllowedItems) {
            Logger.info(
                "[Arceuus Library] LOGIC | Found non-allowed items; will bank " +
                        "(travel items allowed=${script.shouldAllowTravelItems()})."
            )
        }
        return hasNonAllowedItems
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = depositLeaf              // inventory “dirty” → bank

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = travelOrLibraryBranch    // inventory clean → travel vs inside

}