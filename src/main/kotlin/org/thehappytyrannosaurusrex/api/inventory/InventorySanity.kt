package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.rt4.Inventory

/**
 * Helpers for checking whether the current inventory contains items that
 */
object InventorySanity {

    /**
 * Returns true if the inventory contains any item that is not allowed.
 */
    fun hasNonAllowedItems(
        allowedItemIds: Set<Int> = emptySet(),
        allowedByName: List<(String) -> Boolean> = emptyList()
    ): Boolean {
        if (Inventory.isEmpty()) {
            return false
        }

        return Inventory.stream().anyMatch { item ->
            val id = item.id()
            if (id == -1) {
                return@anyMatch false
            }

            val name = item.name()
            val allowedById = id in allowedItemIds
            val allowedByNameMatch = allowedByName.any { predicate -> predicate(name) }

            !(allowedById || allowedByNameMatch)
        }
    }
}
