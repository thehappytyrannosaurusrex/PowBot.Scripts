package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.rt4.Inventory

// Helpers for checking inventory contents against allow-lists
object InventorySanity {

    // Returns true if inventory contains any item not in the allow-list
    fun hasNonAllowedItems(
        allowedItemIds: Set<Int> = emptySet(),
        allowedByName: List<(String) -> Boolean> = emptyList()
    ): Boolean {
        if (Inventory.isEmpty()) return false

        return Inventory.stream().anyMatch { item ->
            val id = item.id()
            if (id == -1) return@anyMatch false

            val name = item.name()
            val allowedById = id in allowedItemIds
            val allowedByNameMatch = allowedByName.any { predicate -> predicate(name) }

            !(allowedById || allowedByNameMatch)
        }
    }
}