package org.thehappytyrannosaurusrex.arceuuslibrary.branches

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.DepositInventoryAtBankLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.InventoryReadyLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.branches.TravelOrLibraryBranch
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * Step 2: Evaluate the inventory.
 *
 * Allowed (always):
 *  - Known Arceuus book item ids
 *  - Any Graceful piece (by name)
 *  - Any stamina potion dose (by name)
 *
 * Allowed (optional; if user enabled "Allow Travel Items"):
 *  - "Teleport" tabs/items (by name snippet)
 *  - Common travel runes (Law, Air, Earth, Water, Fire, Dust, Mist, Mud, Steam, Smoke)
 *  - Xeric’s talisman, Dramen staff (basic mobility helpers)
 *
 * If anything else is present -> bank.
 */
/**
 * InventorySanityBranch: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class InventorySanityBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Inventory sanity check") {

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()

    private val gracefulNames: Set<String> = setOf(
        "Graceful hood", "Graceful top", "Graceful legs",
        "Graceful gloves", "Graceful boots", "Graceful cape"
    )

    private val staminaPrefixes = listOf("Stamina potion(")

    // centralized travel allow-list (by lowercased name)
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
        if (Inventory.isEmpty()) return false

        val hasNonAllowedItems = Inventory.stream().anyMatch { item ->
            val id = item.id()
            if (id == -1) return@anyMatch false
            val name = item.name()
            val ok =
                (id in bookItemIds) ||
                        isGraceful(name) ||
                        isStaminaPotion(name) ||
                        isTravelAllowed(name)
            !ok
        }

        if (hasNonAllowedItems) {
            Logger.info(
                "[InventorySanity] Found non-allowed items; will bank " +
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